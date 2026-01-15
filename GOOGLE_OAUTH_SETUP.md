# Google OAuth Setup & Verification

**Status:** ⚠️ Branding not shown - Needs verification

---

## 🔧 Current Configuration (Production)

### OAuth Credentials
```
Client ID: 499298815524-hlknfbpbv6g0qhighbq2dkbflf2m0rel.apps.googleusercontent.com
Client Secret: GOCSPX-*** (configured)
Status: Active
```

### Redirect URIs
```
✅ https://api-v2.ezami.io/auth/google/callback
```

### Success URLs
```
Mobile: ezami://auth/success
Web: https://ezami.io/api/proxy/auth/google/callback
```

---

## ⚠️ Issues to Resolve

### 1. OAuth Consent Screen Verification

**Required Actions in Google Cloud Console:**

1. **Navigate to:** APIs & Services → OAuth consent screen
2. **Complete all fields:**

#### App Information
- [x] App name: `Ezami`
- [x] User support email: `support@ezami.io`
- [ ] App logo (120x120px) - **REQUIRED**
- [ ] App domain: `ezami.io`

#### App Domain Configuration
- [ ] Application home page: `https://ezami.io`
- [ ] Application privacy policy: `https://ezami.io/privacy`
- [ ] Application terms of service: `https://ezami.io/terms`

#### Developer Contact Information
- [ ] Developer email: `support@ezami.io`

### 2. Authorized Domains
Add to authorized domains list:
- [x] `ezami.io`
- [x] `api-v2.ezami.io`
- [ ] `v2.ezami.io` (if used)
- [ ] `admin.ezami.io` (if needed)

### 3. OAuth Scopes
Required scopes:
- [x] `.../auth/userinfo.email`
- [x] `.../auth/userinfo.profile`
- [x] `openid`

### 4. Test Users (For Testing Status)
While in Testing mode, add test users:
- `hienhv0711@gmail.com`
- `support@ezami.io`
- Other team members

---

## 📋 Verification Checklist

### Before Submitting for Verification

- [ ] App logo uploaded (120x120px, PNG/JPG)
- [ ] Privacy policy live at https://ezami.io/privacy
- [ ] Terms of service live at https://ezami.io/terms
- [ ] All authorized domains added
- [ ] Scopes correctly configured
- [ ] Test users can authenticate
- [ ] Redirect URIs match exactly

### After Submit
- [ ] Wait for Google review (1-7 days)
- [ ] Fix any issues Google identifies
- [ ] Re-submit if rejected

---

## 🔗 Required URLs to Create

### 1. Privacy Policy
**URL:** `https://ezami.io/privacy`

**Must include:**
- What data is collected (email, profile)
- How data is used
- How data is stored
- User rights
- Contact information

### 2. Terms of Service
**URL:** `https://ezami.io/terms`

**Must include:**
- Service description
- User obligations
- Limitation of liability
- Termination policy
- Contact information

### 3. Homepage
**URL:** `https://ezami.io`

**Should have:**
- App description
- Features
- Login/signup buttons
- Link to privacy & terms

---

## 🧪 Testing OAuth Flow

### Test URL (Development)
```
https://api-v2.ezami.io/auth/google/login?platform=web
```

### Expected Flow
1. User clicks "Login with Google"
2. Redirected to Google OAuth consent
3. User authorizes
4. Redirected to callback URL
5. API exchanges code for token
6. Returns JWT to frontend
7. User logged in

### Verify in Browser DevTools
```javascript
// 1. Call login endpoint
fetch('https://api-v2.ezami.io/auth/google/login?platform=web')
  .then(r => r.json())
  .then(d => window.location = d.authorizationUrl)

// 2. After redirect, check callback
// URL should be: https://api-v2.ezami.io/auth/google/callback?code=xxx
```

---

## 🚀 Production Checklist

### Pre-Launch
- [ ] OAuth consent screen verified by Google
- [ ] Privacy policy published
- [ ] Terms of service published
- [ ] Test login flow end-to-end
- [ ] Test on mobile app
- [ ] Test on web app

### Post-Launch
- [ ] Monitor OAuth errors in logs
- [ ] Track successful logins
- [ ] Handle OAuth failures gracefully
- [ ] User feedback on login experience

---

## 📞 Support

**Google Cloud Console:** https://console.cloud.google.com/apis/credentials

**If issues:**
1. Check redirect URI exact match
2. Verify client ID/secret
3. Check authorized domains
4. Review scopes
5. Test with whitelisted email

---

## ✅ Current Status

**Configuration:** ✅ Complete
**Credentials:** ✅ Set in production
**Endpoints:** ✅ Working
**Verification:** ⏳ Pending (needs branding/policies)

**Next Step:** Create privacy policy & terms, upload logo, submit for verification.
