;(function(config) {
    const HtmlWebpackPlugin = require('html-webpack-plugin');
    config.plugins.push(new HtmlWebpackPlugin({
        template: 'kotlin/template.html'
    }))
})(config);