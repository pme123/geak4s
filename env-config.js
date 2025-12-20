// Environment configuration
// This file sets up environment variables BEFORE Scala.js loads
// Vite automatically replaces import.meta.env.VITE_* with actual values

// Make environment variables available globally for Scala.js
// Check if import.meta.env exists (it won't in static HTML files)
if (typeof import.meta !== 'undefined' && import.meta.env) {
  window.GEAK4S_GOOGLE_CLIENTID = import.meta.env.VITE_GEAK4S_GOOGLE_CLIENTID || '';
  window.GEAK4S_GOOGLE_CLIENTSECRET = import.meta.env.VITE_GEAK4S_GOOGLE_CLIENTSECRET || '';
} else {
  // Fallback for static HTML files - these will be replaced by Vite at build time
  window.GEAK4S_GOOGLE_CLIENTID = '';
  window.GEAK4S_GOOGLE_CLIENTSECRET = '';
  console.warn('[env-config] import.meta.env not available - this file needs to be processed by Vite');
}

// Debug: Log to console (remove in production)
console.log('[env-config] GEAK4S_GOOGLE_CLIENTID:', window.GEAK4S_GOOGLE_CLIENTID ? '✓ set' : '✗ not set');
console.log('[env-config] GEAK4S_GOOGLE_CLIENTSECRET:', window.GEAK4S_GOOGLE_CLIENTSECRET ? '✓ set' : '✗ not set');

