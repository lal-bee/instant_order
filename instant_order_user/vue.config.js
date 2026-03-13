const path = require('path')

module.exports = {
  publicPath: '/',
  outputDir: 'dist',
  lintOnSave: false,
  configureWebpack: {
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src'),
      },
    },
  },
  devServer: {
    port: 5174,
    host: '0.0.0.0',
    // 开发时把 /user 代理到后端，避免跨域和手机访问时 localhost 不对的问题
    proxy: {
      '/user': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
    },
  },
}
