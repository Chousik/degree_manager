/*
 * Пример фронтенд-кода на чистом JS для OAuth2 + OIDC потока с PKCE
 * Инклюзивно: 1) отправка логина/пароля 2) генерация PKCE 3) авторизация 4) обмен кода на токены 5) вызов защищённого ресурса
 */

// ----------------------------
// 1. Отправка логина/пароля на Auth Server
async function performLogin(username, password) {
    const resp = await fetch('http://localhost:8071/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ username, password }),
        credentials: 'include' // чтобы получать HttpOnly-cookie
    });

    if (resp.redirected) {
        // После успешного логина Spring Security редиректит на /oauth2/authorize
        window.location.href = resp.url;
    } else if (resp.ok) {
        console.warn('Login response OK but no redirect.');
    } else {
        console.error('Login failed', resp.status);
    }
}

document.getElementById('login-btn').addEventListener('click', () => {
    const userField = document.getElementById('username-input');
    const passField = document.getElementById('password-input');
    performLogin(userField.value, passField.value);
});

// ----------------------------
// 2. Утилиты для PKCE
async function generateCodeVerifier() {
    const array = new Uint8Array(32);
    window.crypto.getRandomValues(array);
    return base64UrlEncode(array);
}

async function generateCodeChallenge(verifier) {
    const encoder = new TextEncoder();
    const data = encoder.encode(verifier);
    const digest = await window.crypto.subtle.digest('SHA-256', data);
    return base64UrlEncode(new Uint8Array(digest));
}

function base64UrlEncode(buffer) {
    return btoa(String.fromCharCode.apply(null, buffer))
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=+$/, '');
}

// ----------------------------
// 3. Редирект на /authorize с PKCE
async function redirectToAuthorize() {
    const codeVerifier = await generateCodeVerifier();
    const codeChallenge = await generateCodeChallenge(codeVerifier);
    const state = Math.random().toString(36).substring(2);

    sessionStorage.setItem('pkce_verifier', codeVerifier);
    sessionStorage.setItem('oauth_state', state);

    const params = new URLSearchParams({
        response_type: 'code',
        client_id: 'client',
        redirect_uri: 'https://your-frontend.com/callback',
        scope: 'openid profile email',
        state: state,
        code_challenge: codeChallenge,
        code_challenge_method: 'S256'
    });

    window.location = `http://localhost:8071/oauth2/authorize?${params}`;
}

// ----------------------------
// 4. Обработка колбэка и обмен кода на токены
async function handleCallback() {
    const url = new URL(window.location.href);
    const code = url.searchParams.get('code');
    const returnedState = url.searchParams.get('state');
    const savedState = sessionStorage.getItem('oauth_state');
    if (returnedState !== savedState) {
        console.error('Invalid state');
        return;
    }

    const codeVerifier = sessionStorage.getItem('pkce_verifier');

    const tokenResponse = await fetch('http://localhost:8071/oauth2/token', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({
            grant_type: 'authorization_code',
            code,
            redirect_uri: 'https://your-frontend.com/callback',
            code_verifier: codeVerifier,
            client_id: 'client'
        }),
        credentials: 'include'
    }).then(res => res.json());

    localStorage.setItem('access_token', tokenResponse.access_token);
    localStorage.setItem('id_token', tokenResponse.id_token);
    // переходим в приложение
    window.location = '/';
}

if (window.location.pathname === '/callback') {
    handleCallback();
}

// ----------------------------
// 5. Вызов защищённого API
async function fetchTasks() {
    const token = localStorage.getItem('access_token');
    const resp = await fetch('http://localhost:8080/api/tasks', {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    const tasks = await resp.json();
    console.log('Tasks:', tasks);
}

document.getElementById('fetch-tasks-btn')?.addEventListener('click', fetchTasks);
