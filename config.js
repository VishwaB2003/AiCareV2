// API base URL — automatically switches between dev and production
// In development (localhost): calls Spring Boot directly
// In production (Netlify): empty string so Netlify's /api proxy handles routing
window.API_BASE = (
  window.location.hostname === 'localhost' ||
  window.location.hostname === '127.0.0.1'
) ? 'http://localhost:8080' : '';
