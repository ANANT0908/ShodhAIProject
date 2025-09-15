/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://backend:8080/api/:path*', // backend service from docker-compose
      },
    ]
  },
};

module.exports = nextConfig;
