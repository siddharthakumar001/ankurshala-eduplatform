/**
 * Content Discovery API Service
 * Provides typed API calls for educational content taxonomy
 * Cascade: Board → Grade → Subject → Chapter → Topic
 */

import { api } from '@/utils/api'

// =========================== TYPE DEFINITIONS ===========================

export interface BoardDropdown {
  id: number
  name: string
}

export interface GradeDropdown {
  id: number
  name: string
  boardId: number
}

export interface SubjectDropdown {
  id: number
  name: string
  gradeId: number
}

export interface ChapterDropdown {
  id: number
  name: string
  subjectId: number
  seq: number
}

export interface TopicDropdown {
  id: number
  title: string
  chapterId: number
  seq: number
}

export interface TopicDetail {
  id: number
  title: string
  summary: string | null
  prerequisites: string | null
  expectedMinutes: number
  chapterId: number
  chapterName: string
  subjectId: number
  subjectName: string
  gradeId: number
  gradeName: string
  boardId: number
  boardName: string
  seq: number
}

export interface BoardDetail {
  id: number
  name: string
  description: string | null
}

export interface GradeDetail {
  id: number
  name: string
  boardId: number
  boardName: string
}

export interface SubjectDetail {
  id: number
  name: string
  gradeId: number
  gradeName: string
  boardId: number
  boardName: string
}

export interface ChapterDetail {
  id: number
  name: string
  subjectId: number
  subjectName: string
  seq: number
}

// =========================== API SERVICE CLASS ===========================

class ContentService {
  /**
   * Get all available boards
   */
  async getBoards(): Promise<BoardDropdown[]> {
    const response = await api.get<BoardDropdown[]>('/content/boards')
    return response.data
  }

  /**
   * Get grades for a specific board
   */
  async getGradesByBoard(boardId: number): Promise<GradeDropdown[]> {
    const response = await api.get<GradeDropdown[]>(`/content/grades/by-board/${boardId}`)
    return response.data
  }

  /**
   * Get subjects for a specific grade
   */
  async getSubjectsByGrade(gradeId: number): Promise<SubjectDropdown[]> {
    const response = await api.get<SubjectDropdown[]>(`/content/subjects/by-grade/${gradeId}`)
    return response.data
  }

  /**
   * Get chapters for a specific subject
   */
  async getChaptersBySubject(subjectId: number): Promise<ChapterDropdown[]> {
    const response = await api.get<ChapterDropdown[]>(`/content/chapters/by-subject/${subjectId}`)
    return response.data
  }

  /**
   * Get topics for a specific chapter
   */
  async getTopicsByChapter(chapterId: number): Promise<TopicDropdown[]> {
    const response = await api.get<TopicDropdown[]>(`/content/topics/by-chapter/${chapterId}`)
    return response.data
  }

  /**
   * Get detailed information about a specific topic
   */
  async getTopicById(topicId: number): Promise<TopicDetail> {
    const response = await api.get<TopicDetail>(`/content/topics/${topicId}`)
    return response.data
  }

  /**
   * Get detailed information about a specific board
   */
  async getBoardById(boardId: number): Promise<BoardDetail> {
    const response = await api.get<BoardDetail>(`/content/boards/${boardId}`)
    return response.data
  }

  /**
   * Get detailed information about a specific grade
   */
  async getGradeById(gradeId: number): Promise<GradeDetail> {
    const response = await api.get<GradeDetail>(`/content/grades/${gradeId}`)
    return response.data
  }

  /**
   * Get detailed information about a specific subject
   */
  async getSubjectById(subjectId: number): Promise<SubjectDetail> {
    const response = await api.get<SubjectDetail>(`/content/subjects/${subjectId}`)
    return response.data
  }

  /**
   * Get detailed information about a specific chapter
   */
  async getChapterById(chapterId: number): Promise<ChapterDetail> {
    const response = await api.get<ChapterDetail>(`/content/chapters/${chapterId}`)
    return response.data
  }

  /**
   * Get full cascade path for a topic (board → grade → subject → chapter → topic)
   * Useful for breadcrumbs and navigation
   */
  async getTopicFullPath(topicId: number): Promise<{
    board: BoardDropdown
    grade: GradeDropdown
    subject: SubjectDropdown
    chapter: ChapterDropdown
    topic: TopicDetail
  }> {
    const topic = await this.getTopicById(topicId)
    
    return {
      board: { id: topic.boardId, name: topic.boardName },
      grade: { id: topic.gradeId, name: topic.gradeName, boardId: topic.boardId },
      subject: { id: topic.subjectId, name: topic.subjectName, gradeId: topic.gradeId },
      chapter: { id: topic.chapterId, name: topic.chapterName, subjectId: topic.subjectId, seq: 0 },
      topic
    }
  }
}

// Export singleton instance
export const contentService = new ContentService()
