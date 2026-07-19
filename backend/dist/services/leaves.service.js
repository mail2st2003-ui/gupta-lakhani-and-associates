"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.LeavesService = void 0;
const leaves_repository_1 = require("../repositories/leaves.repository");
class LeavesService {
    leavesRepository = new leaves_repository_1.LeavesRepository();
    async getLeaves(userUuid) {
        const leaves = await this.leavesRepository.getLeaves(userUuid);
        return leaves.map((l) => {
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
    async checkOverlap(userUuid, startDate, endDate, excludeLeaveUuid) {
        const overlaps = await this.leavesRepository.checkOverlap(userUuid, startDate, endDate, excludeLeaveUuid);
        return overlaps.length > 0;
    }
    async createLeave(leaveData) {
        const dbData = {
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
    async updateLeave(leaveUuid, updateData) {
        const dbData = {};
        if (updateData.status !== undefined)
            dbData.status = updateData.status;
        if (updateData.reason !== undefined)
            dbData.reason = updateData.reason;
        if (updateData.start_date !== undefined)
            dbData.start_date = new Date(updateData.start_date).getTime() || 0;
        if (updateData.end_date !== undefined)
            dbData.end_date = new Date(updateData.end_date).getTime() || 0;
        const result = await this.leavesRepository.updateLeave(leaveUuid, dbData);
        return {
            ...result,
            start_date: new Date(Number(result.start_date)).toISOString().split('T')[0],
            end_date: new Date(Number(result.end_date)).toISOString().split('T')[0]
        };
    }
    async deleteLeave(leaveUuid) {
        return this.leavesRepository.deleteLeave(leaveUuid);
    }
}
exports.LeavesService = LeavesService;
