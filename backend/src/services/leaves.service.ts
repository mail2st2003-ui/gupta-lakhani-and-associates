import { LeavesRepository } from '../repositories/leaves.repository';

export class LeavesService {
  private leavesRepository = new LeavesRepository();

  async getLeaves(userUuid?: string) {
    return this.leavesRepository.getLeaves(userUuid);
  }

  async checkOverlap(userUuid: string, startDate: number, endDate: number) {
    const overlaps = await this.leavesRepository.checkOverlap(userUuid, startDate, endDate);
    return overlaps.length > 0;
  }

  async createLeave(leaveData: any) {
    const dbData: any = {
      uuid: leaveData.uuid || leaveData.id,
      user_uuid: leaveData.user_uuid || leaveData.employeeId,
      reason: leaveData.reason,
      start_date: new Date(leaveData.start_date || leaveData.startDate).getTime() || 0,
      end_date: new Date(leaveData.end_date || leaveData.endDate).getTime() || 0,
      status: leaveData.status || 'Pending'
    };
    return this.leavesRepository.createLeave(dbData);
  }

  async updateLeave(leaveUuid: string, updateData: any) {
    const dbData: any = {};
    if (updateData.status !== undefined) dbData.status = updateData.status;
    if (updateData.reason !== undefined) dbData.reason = updateData.reason;
    if (updateData.start_date !== undefined) dbData.start_date = new Date(updateData.start_date).getTime() || 0;
    if (updateData.end_date !== undefined) dbData.end_date = new Date(updateData.end_date).getTime() || 0;

    return this.leavesRepository.updateLeave(leaveUuid, dbData);
  }

  async deleteLeave(leaveUuid: string) {
    return this.leavesRepository.deleteLeave(leaveUuid);
  }
}
