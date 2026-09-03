export const mockReservationRecords = [
  ['陈思远', 'A-03', '2025-04-24 09:00:00', '2025-04-24 12:00:00', 'APPROVED'],
  ['刘雨欣', 'B-01', '2025-04-24 14:00:00', '2025-04-24 18:00:00', 'APPROVED'],
  ['张伟', 'A-05', '2025-04-25 09:00:00', '2025-04-25 12:00:00', 'APPROVED'],
  ['王芳', 'C-02', '2025-04-25 14:00:00', '2025-04-25 18:00:00', 'CANCELED'],
  ['赵明', 'B-04', '2025-04-25 19:00:00', '2025-04-25 22:00:00', 'APPROVED'],
  ['陈思远', 'A-03', '2025-04-26 09:00:00', '2025-04-26 12:00:00', 'APPROVED'],
  ['刘雨欣', 'B-01', '2025-04-26 14:00:00', '2025-04-26 18:00:00', 'APPROVED'],
  ['张伟', 'A-02', '2025-04-22 09:00:00', '2025-04-22 12:00:00', 'CANCELED'],
].map(([nickName, workstationCode, reserveStart, reserveEnd, status], index) => ({
  reservationId: index + 1,
  userId: 100 + index,
  nickName,
  workstationId: index + 10,
  workstationCode,
  reserveStart,
  reserveEnd,
  status,
}))
