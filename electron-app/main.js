const { app, BrowserWindow, dialog, ipcMain } = require('electron');
const path = require('path');
const express = require('express');
const { spawn } = require('child_process');

const server = express();
const serverPort = 3000;
let backendProcess;

function startBackend() {
    const backendJarPath = app.isPackaged
        ? path.join(process.resourcesPath, 'backend/e12025978-0.0.1-SNAPSHOT.jar')
        : path.join(__dirname, '../backend/target/e12025978-0.0.1-SNAPSHOT.jar');
    backendProcess = spawn('java', ['-jar', backendJarPath]);

    backendProcess.stdout.on('data', (data) => {
        console.log(`Backend stdout: ${data}`);
    });

    backendProcess.stderr.on('data', (data) => {
        console.error(`Backend stderr: ${data}`);
    });

    backendProcess.on('close', (code) => {
        console.log(`Backend process exited with code ${code}`);
    });

    console.log('Backend process started on port 8080');
}

function createWindow() {
    const mainWindow = new BrowserWindow({
        width: 1400,
        height: 800,
        fullscreen: true,
        webPreferences: {
            nodeIntegration: true,
            contextIsolation: false,
            webSecurity: false, // Disable web security to allow local file URLs
        },
    });

    // Load the Angular application from the local server
    mainWindow.loadURL(`http://localhost:${serverPort}`).catch(err => console.log('Failed to load URL:', err));

    // Open the DevTools (optional)
    // mainWindow.webContents.openDevTools();
}

app.on('ready', () => {
    // Start the backend server if packed
    if (app.isPackaged) {
        startBackend();
    }

    // Determine the correct path to the dist directory
    const distPath = app.isPackaged
        ? path.join(process.resourcesPath, 'dist/artwork-authenticator')
        : path.join(__dirname, '../frontend/dist/artwork-authenticator');

    console.log('distPath:', distPath);

    // Serve static files from the dist directory
    server.use(express.static(distPath));

    // Send the index.html file for any route
    server.get('*', (req, res) => {
        res.sendFile(path.join(distPath, 'index.html'));
    });

    // Start the server
    server.listen(serverPort, () => {
        console.log(`Server running at http://localhost:${serverPort}`);
        createWindow();
    });
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

// Handle IPC event for opening file dialog
ipcMain.on('open-file-dialog', (event) => {
    dialog.showOpenDialog({
        properties: ['openFile'],
        filters: [{ name: 'Images', extensions: ['jpg', 'png'] }]
    }).then(result => {
        if (!result.canceled) {
            event.sender.send('selected-file', result.filePaths[0]);
        }
    }).catch(err => {
        console.log(err);
    });
});
