package com.example.data

import kotlinx.coroutines.flow.Flow

class StudyRepository(private val studyDao: StudyDao) {
    val allSessions: Flow<List<StudySession>> = studyDao.getAllSessions()
    val totalStudyMinutes: Flow<Int?> = studyDao.getTotalStudyMinutes()

    suspend fun insert(session: StudySession) = studyDao.insertSession(session)
}
