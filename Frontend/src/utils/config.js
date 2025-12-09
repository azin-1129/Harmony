// src/utils/config.js
export const BACKEND_URL = import.meta.env.VITE_BACKEND_URL || ''

// helper to build full backend URLs. Expect `path` to start with '/'
export const apiUrl = (path) => `${BACKEND_URL}${path}`
