import { LeavesRepository } from '../repositories/leaves.repository';

export class LeavesService {
  private leavesRepository = new LeavesRepository();

  async getLeaves(userUuid?: string) {
    const leaves = await this.leavesRepository.getLeaves(userUuid);
    return leaves.map((l: any) => {
      // Safely convert BIGINT timestamp back to YYYY-MM-DD string
      const start = new Date(Number(l.start_date));
      const end = new Date(Number(l.end_date));
      return {
        ...l,
        start_date: start.toISOString().split('T')[0],
        end_date: end.toISOString().split('T')[0]
      };
    });
  }

  async checkOverlap(userUuid: string, startDate: number, endDate: number, excludeLeaveUuid?: string) {
    const overlaps = await this.leavesRepository.checkOverlap(userUuid, startDate, endDate, excludeLeaveUuid);
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
    const result = await this.leavesRepository.createLeave(dbData);
    return {
      ...result,
      start_date: new Date(Number(result.start_date)).toISOString().split('T')[0],
      end_date: new Date(Number(result.end_date)).toISOString().split('T')[0]
    };
  }

  async updateLeave(leaveUuid: string, updateData: any) {
    const dbData: any = {};
    if (updateData.status !== undefined) dbData.status = updateData.status;
    if (updateData.reason !== undefined) dbData.reason = updateData.reason;
    if (updateData.start_date !== undefined) dbData.start_date = new Date(updateData.start_date).getTime() || 0;
    if (updateData.end_date !== undefined) dbData.end_date = new Date(updateData.end_date).getTime() || 0;

    const result = await this.leavesRepository.updateLeave(leaveUuid, dbData);
    return {
      ...result,
      start_date: new Date(Number(result.start_date)).toISOString().split('T')[0],
      end_date: new Date(Number(result.end_date)).toISOString().split('T')[0]
    };
  }

  async deleteLeave(leaveUuid: string) {
    return this.leavesRepository.deleteLeave(leaveUuid);
  }
}
