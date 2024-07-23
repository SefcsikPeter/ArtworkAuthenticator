const { app, BrowserWindow, dialog, ipcMain } = require('electron');
const path = require('path');
const express = require('express');
const { spawn } = require('child_process');
const fs = require("fs");

const server = express();
const serverPort = 3000;
let backendProcess;

function startBackend() {
    return new Promise((resolve, reject) => {
        const backendJarPath = app.isPackaged
            ? path.join(process.resourcesPath, 'backend/e12025978-0.0.1-SNAPSHOT.jar')
            : path.join(__dirname, '../backend/target/e12025978-0.0.1-SNAPSHOT.jar');

        backendProcess = spawn('java', ['-jar', backendJarPath]);

        backendProcess.stdout.on('data', (data) => {
            const message = data.toString();
            console.log(`Backend stdout: ${message}`);

            if (message.includes('Started ArtworkAuthenticatorApplication')) {
                resolve();
            } else if (message.includes('Port 8080 was already in use')) {
                reject(new Error('Port 8080 was already in use'));
            } else if (message.includes('KeyPairGenerator not available')) {
                reject(new Error('KeyPairGenerator not available'));
            } else if (message.includes('Could not save RSA keys')) {
                reject(new Error('Could not save RSA keys'));
            }
        });

        backendProcess.on('close', (code) => {
            console.log(`Backend process exited with code ${code}`);
            if (code !== 0) {
                reject(new Error(`Backend process exited with code ${code}`));
            }
        });

        console.log('Backend process started on port 8080');
    });
}

function createWindow() {
    const mainWindow = new BrowserWindow({
        width: 1400,
        height: 800,
        fullscreen: true,
        webPreferences: {
            nodeIntegration: true,
            contextIsolation: false,
            webSecurity: true,
        },
    });

    // Load the Angular application from the local server
    mainWindow.loadURL(`http://localhost:${serverPort}`).catch(err => console.log('Failed to load URL:', err));

    //mainWindow.webContents.openDevTools();
}

app.on('ready', () => {
    // Determine the correct path to the dist directory
    const distPath = app.isPackaged
        ? path.join(process.resourcesPath, 'dist/artwork-authenticator')
        : path.join(__dirname, '../frontend/dist/artwork-authenticator');

    console.log('distPath:', distPath);

    // Serve static files from the dist directory
    server.use(express.static(distPath));

    server.get('/image/:path', (req, res) => {
        try {
            const filePath = Buffer.from(decodeURIComponent(req.params.path), 'base64').toString('utf8');
            // Stream the file to the response
            const readStream = fs.createReadStream(filePath);
            readStream.on('error', error => {
                res.status(404).send('Image not found');
            });
            res.setHeader('Content-Type', 'image/jpg'); // Set appropriate content type if known, or make it dynamic
            readStream.pipe(res);
        } catch (error) {
            res.status(500).send('Failed to process image');
        }
    });

    // Send the index.html file for any route
    server.get('*', (req, res) => {
        res.sendFile(path.join(distPath, 'index.html'));
    });

    // Start the server with error handling
    const serverInstance = server.listen(serverPort, () => {
        console.log(`Server running at http://localhost:${serverPort}`);

        // Start the backend process
        startBackend()
            .then(() => {
                // Create the window once the backend has started
                createWindow();
            })
            .catch((error) => {
                console.error('Failed to start backend:', error);
                let errorMessage = error.message;

                if (error.message.includes('Port 8080 was already in use')) {
                    errorMessage = 'The port 8080 is already in use. Please close the application using this port.';
                }

                if (error.message.includes('KeyPairGenerator not available')) {
                    errorMessage = 'KeyPairGenerator not available';
                }

                if (error.message.includes('Could not save RSA keys')) {
                    errorMessage = 'Could not save RSA keys';
                }

                dialog.showErrorBox('Backend Error', errorMessage);
                setTimeout(() => app.quit(), 1000); // Delay quit to allow the dialog to show
            });
    });

    serverInstance.on('error', (error) => {
        if (error.code === 'EADDRINUSE') {
            dialog.showErrorBox('Port In Use', `The port ${serverPort} is already in use. Please close the application using this port.`);
            setTimeout(() => app.quit(), 1000); // Delay quit to allow the dialog to show
        } else {
            dialog.showErrorBox('Server Error', `An unexpected error occurred: ${error.message}`);
            setTimeout(() => app.quit(), 1000); // Delay quit to allow the dialog to show
        }
    });
});

// Global error handler to catch uncaught exceptions and unhandled promise rejections
process.on('uncaughtException', (error) => {
    dialog.showErrorBox('An Error Occurred', `An unexpected error occurred: ${error.message}`);
    console.error('Uncaught Exception:', error);
    setTimeout(() => app.quit(), 1000); // Delay quit to allow the dialog to show
});

process.on('unhandledRejection', (reason, promise) => {
    dialog.showErrorBox('An Error Occurred', `An unexpected error occurred: ${reason.message}`);
    console.error('Unhandled Rejection at:', promise, 'reason:', reason);
    setTimeout(() => app.quit(), 1000); // Delay quit to allow the dialog to show
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') {
        app.quit();
    }
});

app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
        createWindow();
    }
});

app.on('will-quit', () => {
    if (backendProcess) {
        backendProcess.kill();
    }
});

ipcMain.on('open-file-dialog', (event) => {
    dialog.showOpenDialog({
        properties: ['openFile'],
        filters: [{ name: 'Images', extensions: ['jpg', 'png', 'jpeg'] }]
    }).then(result => {
        if (!result.canceled) {
            const encodedPath = encodeURIComponent(Buffer.from(result.filePaths[0]).toString('base64'));
            event.sender.send('selected-file', encodedPath);
        }
    }).catch(err => {
        console.log(err);
    });
});
