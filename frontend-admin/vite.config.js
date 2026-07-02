import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import { resolve } from 'path'
import viteCompression from 'vite-plugin-compression'

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd())

    // 运行时配置注入：从 .env.* 读取，通过 define 注入到 window
    const runtimeConfig = {
        baseUrl: env.VITE_APP_BASE_URL || '/jeeStudio/gtoa/a/',
        serverUrl: env.VITE_APP_SERVER_URL || '',
        fileHost: env.VITE_APP_FILE_HOST || '',
        documentServerUrl: env.VITE_APP_DOCUMENT_SERVER_URL || '',
        onlyofficeInnerUrl: env.VITE_APP_ONLYOFFICE_INNER_URL || '',
        cookiePrefix: env.VITE_APP_COOKIE_PREFIX || 'jeeStudio_ant_vue3',
        tokenKey: env.VITE_APP_TOKEN_KEY || '',
        tokenName: env.VITE_APP_TOKEN_NAME || 'token',
    }

    return {
        base: '/bpm/',
        plugins: [
            vue(),
            vueJsx(),
            viteCompression({
                threshold: 10240,
                algorithm: 'gzip',
                ext: '.gz',
            }),
        ],
        resolve: {
            alias: {
                '@': resolve(__dirname, 'src'),
            },
            extensions: ['.mjs', '.js', '.mts', '.ts', '.jsx', '.tsx', '.json', '.vue'],
        },
        css: {
            preprocessorOptions: {
                less: {
                    javascriptEnabled: true,
                },
            },
        },
        server: {
            proxy: {
                '/jeeStudio': {
                    target: env.VITE_PROXY_BACKEND,
                    changeOrigin: true,
                },
                '/bpm/jeeStudio': {
                    target: env.VITE_PROXY_BACKEND,
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/bpm/, ''),
                },
                '/minio': {
                    target: env.VITE_PROXY_MINIO,
                    rewrite: (path) => path.replace(/^\/minio/, ''),
                    changeOrigin: true,
                },
            },
        },
        build: {
            sourcemap: false,
            chunkSizeWarningLimit: 2000,
            commonjsOptions: {
                include: [/node_modules/, /src\/lib\//],
                transformMixedEsModules: true,
            },
        },
        esbuild: {
            loader: 'jsx',
            include: /src\/.*\.js$/,
        },
        optimizeDeps: {
            include: [
                'ant-design-vue',
                'dayjs',
                'axios',
                'crypto-js',
                'leaflet',
                'leaflet.markercluster',
            ],
            esbuildOptions: {
                loader: {
                    '.js': 'jsx',
                },
            },
            entries: ['index.html'],
        },
        define: {
            'process.env.VUE_APP_BUILD_TIME': JSON.stringify(Date.now().toString()),
            '__RUNTIME_CONFIG__': JSON.stringify(runtimeConfig),
        },
    }
})
