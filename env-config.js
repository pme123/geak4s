// Environment configuration
// This file sets up environment variables BEFORE Scala.js loads
// Vite automatically replaces import.meta.env.VITE_* with actual values at build time

// Make environment variables available globally for Scala.js
// Vite will replace import.meta.env.VITE_* with the actual values during build
window.GEAK4S_GOOGLE_CLIENTID = import.meta.env.VITE_GEAK4S_GOOGLE_CLIENTID || '';
window.GEAK4S_GOOGLE_CLIENTSECRET = import.meta.env.VITE_GEAK4S_GOOGLE_CLIENTSECRET || '';

// Debug: Log to console
console.log('[env-config] Environment variables loaded:');
console.log('[env-config] GEAK4S_GOOGLE_CLIENTID:', window.GEAK4S_GOOGLE_CLIENTID ? '✓ set' : '✗ not set');
console.log('[env-config] GEAK4S_GOOGLE_CLIENTSECRET:', window.GEAK4S_GOOGLE_CLIENTSECRET ? '✓ set' : '✗ not set');

