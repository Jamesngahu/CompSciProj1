// Base URL of the deployed backend (Railway). Update this after you deploy the backend.
// Locally, run the Spring Boot app on port 9090 and this default will work as-is.
const API_BASE = (location.hostname === 'localhost' || location.hostname === '127.0.0.1')
    ? 'http://localhost:9090'
    : 'https://CHANGE-ME.up.railway.app';
