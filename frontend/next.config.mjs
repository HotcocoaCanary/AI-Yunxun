const nextConfig = {
  /* config options here */
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
    ];
  },
  webpack: (config, { dev }) => {
    if (dev) {
      const ignored = [
        '**/pagefile.sys',
        '**/swapfile.sys',
        '**/hiberfil.sys',
        '**/DumpStack.log.tmp',
      ];
      const existingSource = config.watchOptions?.ignored;
      const existing =
        typeof existingSource === 'string'
          ? [existingSource]
          : Array.isArray(existingSource)
            ? existingSource.filter((item) => typeof item === 'string')
            : [];
      config.watchOptions = {
        ...(config.watchOptions || {}),
        ignored: [...existing, ...ignored],
      };
    }
    return config;
  },
};

export default nextConfig;
