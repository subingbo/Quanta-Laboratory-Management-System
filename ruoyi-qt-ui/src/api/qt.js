import request from '@/utils/request'

export const listActivities = params => request({ url: '/system/activity/list', method: 'get', params })
export const getActivity = activityId => request({ url: '/system/activity/' + activityId, method: 'get' })
export const signupActivity = data => request({ url: '/system/signup', method: 'post', data })
export const listMySignups = params => request({ url: '/system/signup/detailList', method: 'get', params })

export const listBooks = params => request({ url: '/system/book/list', method: 'get', params })
export const borrowBook = data => request({ url: '/system/borrow', method: 'post', data })
export const listBookBorrows = params => request({ url: '/system/borrow/detailList', method: 'get', params })

export const listWorkstations = params => request({ url: '/system/workstation/list', method: 'get', params })
export const reserveWorkstation = data => request({ url: '/system/reservation', method: 'post', data })
export const listReservations = params => request({ url: '/system/reservation/detailList', method: 'get', params })

export const listClothingItems = params => request({ url: '/system/item/list', method: 'get', params })
export const getClothingItem = itemId => request({ url: '/system/item/' + itemId, method: 'get' })
export const listPaymentConfigs = params => request({ url: '/system/config/list', method: 'get', params })
export const createClothingOrder = data => request({ url: '/system/order', method: 'post', data })
export const listClothingOrders = params => request({ url: '/system/order/detailList', method: 'get', params })

export const getProfile = () => request({ url: '/system/user/profile', method: 'get' })
export const updateProfile = data => request({ url: '/system/user/profile', method: 'put', data })
export const updatePassword = data => request({ url: '/system/user/profile/updatePwd', method: 'put', data })
export const listUsers = params => request({ url: '/system/user/list', method: 'get', params })

export const applyInterview = data => request({ url: '/qt/interview/apply', method: 'post', data })
export const getMyInterviewApplication = () => request({ url: '/qt/interview/my', method: 'get' })
export const getMyInterviewResults = () => request({ url: '/qt/interview/myResults', method: 'get' })
