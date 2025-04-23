function base64urlEncode(str) {
    return btoa(String.fromCharCode(...new Uint8Array(str)))
        .replace(/\+/g, '-')
        .replace(/\//g, '_')
        .replace(/=+$/, '')
}

export async function generatePKCE() {
    const codeVerifier = [...Array(64)].map(() => Math.random().toString(36)[2]).join('')
    const encoder = new TextEncoder()
    const data = encoder.encode(codeVerifier)
    const hashBuffer = await crypto.subtle.digest('SHA-256', data)
    const codeChallenge = base64urlEncode(hashBuffer)

    return { codeVerifier, codeChallenge }
}
