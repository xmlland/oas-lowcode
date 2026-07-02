/**
 * Login page quick account list.
 * The backend demo login accepts this placeholder password.
 */
const getPwd = () => {
  return 'xxx'
}

export default {
  userList: [
    { showText: 'admin', username: 'admin', password: getPwd() },
    { showText: 'dingsl', username: 'dingsl', password: getPwd() },
  ],
}
