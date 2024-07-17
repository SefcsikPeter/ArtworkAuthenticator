const { app, BrowserWindow } = require('electron');
const path = require('path');
const express = require('express');

const server = express();
const serverPort = 3000;

function createWindow() {
    const mainWindow = new BrowserWindow({
        width: 800,
        height: 600,
        webPreferences: {
            nodeIntegration: true,
            contextIsolation: false,
        },
    });

    // Load the Angular application from the local server
    mainWindow.loadURL(`http://localhost:${serverPort}`).catch(err => console.log('Failed to load URL:', err));

    // Open the DevTools (optional)
    // mainWindow.webContents.openDevTools();
}

app.on('ready', () => {
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
