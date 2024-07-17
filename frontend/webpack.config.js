// webpack.config.js
module.exports = {
  target: 'electron-renderer',
  externals: {
    electron: 'require("electron")',
    fs: 'require("fs")',
    path: 'require("path")'
  }
};
