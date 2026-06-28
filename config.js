// API base URL — switches automatically between dev and production
// After deploying backend to Elastic Beanstalk, replace the URL below
// with your actual EB environment URL (e.g. http://aicare.us-east-1.elasticbeanstalk.com)
const PROD_API = 'http://YOUR_EB_ENV.elasticbeanstalk.com';

window.API_BASE = (
  window.location.hostname === 'localhost' ||
  window.location.hostname === '127.0.0.1'
) ? 'http://localhost:8080' : PROD_API;
