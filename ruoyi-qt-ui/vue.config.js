const path = require('path')

function resolve(dir) {
  return path.join(__dirname, dir)
}

const target = process.env.VUE_APP_API_TARGET || 'http://localhost:8080'

module.exports = {
  publicPath: '/',
  outputDir: 'dist',
  assetsDir: 'static',
  productionSourceMap: false,
  devServer: {
    host: '0.0.0.0',
    port: process.env.PORT || 81,
    open: false,
    proxy: {
      [process.env.VUE_APP_BASE_API || '/dev-api']: {
        target,
        changeOrigin: true,
        pathRewrite: {
          ['^' + (process.env.VUE_APP_BASE_API || '/dev-api')]: ''
        }
      }
    },
    disableHostCheck: true
  },
  configureWebpack: {
    name: 'Quanta',
    resolve: {
      alias: {
        '@': resolve('src')
      }
    }
  },
  css: {
    loaderOptions: {
      sass: {
        sassOptions: { outputStyle: 'expanded' }
      }
    }
  }
}
