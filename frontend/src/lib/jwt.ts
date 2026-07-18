/** Minimal JWT payload decoder (no verification — the backend validates the signature). */
export interface JwtPayload {
  sub?: string;
  exp?: number;
  iat?: number;
  [key: string]: unknown;
}

export function decodeJwt(token: string): JwtPayload | null {
  try {
    const [, payload] = token.split(".");
    if (!payload) return null;
    const json = atob(payload.replace(/-/g, "+").replace(/_/g, "/"));
    return JSON.parse(json) as JwtPayload;
  } catch {
    return null;
  }
}

/** True when the token is missing, malformed, or past its `exp` (with a small clock-skew buffer). */
export function isTokenExpired(token: string | null): boolean {
  if (!token) return true;
  const payload = decodeJwt(token);
  if (!payload?.exp) return true;
  const nowSeconds = Date.now() / 1000;
  return payload.exp < nowSeconds - 5;
}
