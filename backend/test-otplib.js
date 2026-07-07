const { TOTP } = require('otplib');
const totp = new TOTP();
console.log(totp.generateSecret());
