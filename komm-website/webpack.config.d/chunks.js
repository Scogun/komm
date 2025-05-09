config.output.publicPath = '/'
config.optimization = {
    splitChunks: {
        cacheGroups: {
            vendor: {
                test: /[\\/]node_modules[\\/]/,
                name: 'vendor',
                chunks: 'all',
            },
            kotlin: {
                test: /[\\/]kotlin[\\/](kotlin-kotlin|korlibs)/,
                name: 'kotlin',
                chunks: 'all',
            },
            kotlinx: {
                test: /[\\/]kotlin[\\/](kotlinx)/,
                name: 'kotlinx',
                chunks: 'all',
            },
        }
    }
};