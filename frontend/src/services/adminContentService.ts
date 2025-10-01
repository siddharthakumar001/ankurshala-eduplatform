/**
 * Admin Content Management API Service
 * Provides typed API calls for all admin content management operations
 */

import { api } from '@/utils/api'

// =========================== TYPE DEFINITIONS ===========================

export interface PageRequest {
  page?: number
  size?: number
  sortBy?: string
  sortDir?: 'asc' | 'desc'
  search?: string
  active?: boolean
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
  numberOfElements: number
  empty: boolean
}

// Entity DTOs
export interface BoardDto {
  id: number
  name: string
  active: boolean
  softDeleted?: boolean
  createdAt: string
  updatedAt: string
  gradesCount?: number
  subjectsCount?: number
}

export interface GradeDto {
  id: number
  name: string
  displayName: string
  boardId: number
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface SubjectDto {
  id: number
  name: string
  active: boolean
  boardId: number
  gradeId: number
  createdAt: string
  updatedAt: string
}

export interface ChapterDto {
  id: number
  name: string
  subjectId: number
  subjectName?: string
  boardId: number
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface TopicDto {
  id: number
  title: string
  code?: string
  description?: string
  summary?: string
  expectedTimeMins?: number
  chapterId: number
  chapterName?: string
  subjectName?: string
  boardId: number
  subjectId: number
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface TopicNoteDto {
  id: number
  title: string
  content: string
  topicId: number
  topicTitle?: string
  attachments?: string
  active: boolean
  createdAt: string
  updatedAt: string
  // Hierarchy fields for dropdown
  boardId?: number
  gradeId?: number
  subjectId?: number
  chapterId?: number
}

// Dropdown DTOs
export interface BoardDropdownDto {
  id: number
  name: string
}

export interface GradeDropdownDto {
  id: number
  name: string
  displayName: string
  boardId: number
}

export interface SubjectDropdownDto {
  id: number
  name: string
  boardId: number
  gradeId: number
}

export interface ChapterDropdownDto {
  id: number
  name: string
  subjectId: number
  boardId: number
}

export interface TopicDropdownDto {
  id: number
  title: string
  chapterId: number
  subjectId: number
}

// Request DTOs
export interface CreateBoardRequest {
  name: string
  active: boolean
}

export interface UpdateBoardRequest {
  name: string
  active?: boolean
}

export interface CreateGradeRequest {
  name: string
  displayName: string
  boardId: number
  active: boolean
}

export interface UpdateGradeRequest {
  name: string
  displayName: string
  active?: boolean
}

export interface CreateSubjectRequest {
  name: string
  boardId: number
  gradeId: number
  active: boolean
}

export interface UpdateSubjectRequest {
  name: string
  active?: boolean
}

export interface CreateChapterRequest {
  name: string
  boardId: number
  gradeId: number
  subjectId: number
  active: boolean
}

export interface UpdateChapterRequest {
  name: string
  subjectId: number
  active?: boolean
}

export interface CreateTopicRequest {
  title: string
  description?: string
  summary?: string
  expectedTimeMins?: number
  boardId: number
  gradeId: number
  subjectId: number
  chapterId: number
  active: boolean
}

export interface UpdateTopicRequest {
  title: string
  description?: string
  summary?: string
  expectedTimeMins?: number
  boardId: number
  subjectId: number
  chapterId: number
  active?: boolean
}

export interface CreateTopicNoteRequest {
  title: string
  content: string
  topicId: number
  attachments?: string
  active: boolean
  // Hierarchy for easier form management
  boardId?: number
  gradeId?: number
  subjectId?: number
  chapterId?: number
}

export interface UpdateTopicNoteRequest {
  title: string
  content: string
  attachments?: string
  active?: boolean
}

export interface DeletionImpactDto {
  canDelete: boolean
  totalImpact: number
  warnings: string[]
  // Specific counts
  boardName?: string
  boardId?: number
  gradesCount?: number
  subjectsCount?: number
  chaptersCount?: number
  topicsCount?: number
  notesCount?: number
}

export interface ContentTreeDto {
  id?: number
  name?: string
  type?: string
  active?: boolean
  children?: ContentTreeDto[]
  // Flat data access
  boards?: BoardDto[]
  grades?: GradeDto[]
  subjects?: SubjectDto[]
  chapters?: ChapterDto[]
  topics?: TopicDto[]
  topicNotes?: TopicNoteDto[]
}

// =========================== API SERVICE CLASS ===========================

class AdminContentService {
  private baseUrl = '/admin/content'
  private utilsUrl = '/admin/content-utils'

  // ============ BOARD OPERATIONS ============
  
  async getBoards(params: PageRequest = {}): Promise<PageResponse<BoardDto>> {
    const searchParams = new URLSearchParams()
    if (params.page !== undefined) searchParams.append('page', params.page.toString())
    if (params.size !== undefined) searchParams.append('size', params.size.toString())
    if (params.sortBy) searchParams.append('sortBy', params.sortBy)
    if (params.sortDir) searchParams.append('sortDir', params.sortDir)
    if (params.search) searchParams.append('search', params.search)
    if (params.active !== undefined) searchParams.append('active', params.active.toString())
    
    const url = `${this.baseUrl}/boards?${searchParams}`
    console.log('AdminContentService: Making API call to:', url)
    
    try {
      const response = await api.get<PageResponse<BoardDto>>(url)
      console.log('AdminContentService: API response:', response)
      return response.data
    } catch (error) {
      console.error('AdminContentService: API error:', error)
      throw error
    }
  }

  async createBoard(request: CreateBoardRequest): Promise<BoardDto> {
    const response = await api.post<BoardDto>(`${this.baseUrl}/boards`, request)
    return response.data
  }

  async updateBoard(id: number, request: UpdateBoardRequest): Promise<BoardDto> {
    const response = await api.put<BoardDto>(`${this.baseUrl}/boards/${id}`, request)
    return response.data
  }

  async updateBoardStatus(id: number, active: boolean): Promise<BoardDto> {
    const response = await api.patch<BoardDto>(`${this.baseUrl}/boards/${id}/status`, { active })
    return response.data
  }

  async getBoardDeletionImpact(id: number): Promise<DeletionImpactDto> {
    const response = await api.get<DeletionImpactDto>(`${this.baseUrl}/boards/${id}/deletion-impact`)
    return response.data
  }

  async deleteBoard(id: number, force: boolean = false): Promise<any> {
    const response = await api.delete(`${this.baseUrl}/boards/${id}?force=${force}`)
    return response.data
  }

  // ============ GRADE OPERATIONS ============

  async getGrades(params: PageRequest & { boardId?: number } = {}): Promise<PageResponse<GradeDto>> {
    const searchParams = new URLSearchParams()
    if (params.page !== undefined) searchParams.append('page', params.page.toString())
    if (params.size !== undefined) searchParams.append('size', params.size.toString())
    if (params.sortBy) searchParams.append('sortBy', params.sortBy)
    if (params.sortDir) searchParams.append('sortDir', params.sortDir)
    if (params.search) searchParams.append('search', params.search)
    if (params.active !== undefined) searchParams.append('active', params.active.toString())
    if (params.boardId) searchParams.append('boardId', params.boardId.toString())
    
    const response = await api.get<PageResponse<GradeDto>>(`${this.baseUrl}/grades?${searchParams}`)
    return response.data
  }

  async createGrade(request: CreateGradeRequest): Promise<GradeDto> {
    const response = await api.post<GradeDto>(`${this.baseUrl}/grades`, request)
    return response.data
  }

  async updateGrade(id: number, request: UpdateGradeRequest): Promise<GradeDto> {
    const response = await api.put<GradeDto>(`${this.baseUrl}/grades/${id}`, request)
    return response.data
  }

  async updateGradeStatus(id: number, active: boolean): Promise<GradeDto> {
    const response = await api.patch<GradeDto>(`${this.baseUrl}/grades/${id}/status`, { active })
    return response.data
  }

  async deleteGrade(id: number, force: boolean = false): Promise<any> {
    const response = await api.delete(`${this.baseUrl}/grades/${id}?force=${force}`)
    return response.data
  }

  // ============ SUBJECT OPERATIONS ============

  async getSubjects(params: PageRequest & { boardId?: number; gradeId?: number } = {}): Promise<PageResponse<SubjectDto>> {
    const searchParams = new URLSearchParams()
    if (params.page !== undefined) searchParams.append('page', params.page.toString())
    if (params.size !== undefined) searchParams.append('size', params.size.toString())
    if (params.sortBy) searchParams.append('sortBy', params.sortBy)
    if (params.sortDir) searchParams.append('sortDir', params.sortDir)
    if (params.search) searchParams.append('search', params.search)
    if (params.active !== undefined) searchParams.append('active', params.active.toString())
    if (params.boardId) searchParams.append('boardId', params.boardId.toString())
    if (params.gradeId) searchParams.append('gradeId', params.gradeId.toString())
    
    const response = await api.get<PageResponse<SubjectDto>>(`${this.baseUrl}/subjects?${searchParams}`)
    return response.data
  }

  async createSubject(request: CreateSubjectRequest): Promise<SubjectDto> {
    const response = await api.post<SubjectDto>(`${this.baseUrl}/subjects`, request)
    return response.data
  }

  async updateSubject(id: number, request: UpdateSubjectRequest): Promise<SubjectDto> {
    const response = await api.put<SubjectDto>(`${this.baseUrl}/subjects/${id}`, request)
    return response.data
  }

  async updateSubjectStatus(id: number, active: boolean): Promise<SubjectDto> {
    const response = await api.patch<SubjectDto>(`${this.baseUrl}/subjects/${id}/status`, { active })
    return response.data
  }

  async getSubjectDeletionImpact(id: number): Promise<DeletionImpactDto> {
    const response = await api.get<DeletionImpactDto>(`${this.baseUrl}/subjects/${id}/deletion-impact`)
    return response.data
  }

  async deleteSubject(id: number, force: boolean = false): Promise<any> {
    const response = await api.delete(`${this.baseUrl}/subjects/${id}?force=${force}`)
    return response.data
  }

  // ============ CHAPTER OPERATIONS ============

  async getChapters(params: PageRequest & { subjectId?: number } = {}): Promise<PageResponse<ChapterDto>> {
    const searchParams = new URLSearchParams()
    if (params.page !== undefined) searchParams.append('page', params.page.toString())
    if (params.size !== undefined) searchParams.append('size', params.size.toString())
    if (params.sortBy) searchParams.append('sortBy', params.sortBy)
    if (params.sortDir) searchParams.append('sortDir', params.sortDir)
    if (params.search) searchParams.append('search', params.search)
    if (params.active !== undefined) searchParams.append('active', params.active.toString())
    if (params.subjectId) searchParams.append('subjectId', params.subjectId.toString())
    
    const response = await api.get<PageResponse<ChapterDto>>(`${this.baseUrl}/chapters?${searchParams}`)
    return response.data
  }

  async createChapter(request: CreateChapterRequest): Promise<ChapterDto> {
    const response = await api.post<ChapterDto>(`${this.baseUrl}/chapters`, request)
    return response.data
  }

  async updateChapter(id: number, request: UpdateChapterRequest): Promise<ChapterDto> {
    const response = await api.put<ChapterDto>(`${this.baseUrl}/chapters/${id}`, request)
    return response.data
  }

  async updateChapterStatus(id: number, active: boolean): Promise<ChapterDto> {
    const response = await api.patch<ChapterDto>(`${this.baseUrl}/chapters/${id}/status`, { active })
    return response.data
  }

  async getChapterDeletionImpact(id: number): Promise<DeletionImpactDto> {
    const response = await api.get<DeletionImpactDto>(`${this.baseUrl}/chapters/${id}/deletion-impact`)
    return response.data
  }

  async deleteChapter(id: number, force: boolean = false): Promise<any> {
    const response = await api.delete(`${this.baseUrl}/chapters/${id}?force=${force}`)
    return response.data
  }

  // ============ TOPIC OPERATIONS ============

  async getTopics(params: PageRequest & { chapterId?: number; subjectId?: number } = {}): Promise<PageResponse<TopicDto>> {
    const searchParams = new URLSearchParams()
    if (params.page !== undefined) searchParams.append('page', params.page.toString())
    if (params.size !== undefined) searchParams.append('size', params.size.toString())
    if (params.sortBy) searchParams.append('sortBy', params.sortBy)
    if (params.sortDir) searchParams.append('sortDir', params.sortDir)
    if (params.search) searchParams.append('search', params.search)
    if (params.active !== undefined) searchParams.append('active', params.active.toString())
    if (params.chapterId) searchParams.append('chapterId', params.chapterId.toString())
    if (params.subjectId) searchParams.append('subjectId', params.subjectId.toString())
    
    const response = await api.get<PageResponse<TopicDto>>(`${this.baseUrl}/topics?${searchParams}`)
    return response.data
  }

  async createTopic(request: CreateTopicRequest): Promise<TopicDto> {
    const response = await api.post<TopicDto>(`${this.baseUrl}/topics`, request)
    return response.data
  }

  async updateTopic(id: number, request: UpdateTopicRequest): Promise<TopicDto> {
    const response = await api.put<TopicDto>(`${this.baseUrl}/topics/${id}`, request)
    return response.data
  }

  async updateTopicStatus(id: number, active: boolean): Promise<TopicDto> {
    const response = await api.patch<TopicDto>(`${this.baseUrl}/topics/${id}/status`, { active })
    return response.data
  }

  async getTopicDeletionImpact(id: number): Promise<DeletionImpactDto> {
    const response = await api.get<DeletionImpactDto>(`${this.baseUrl}/topics/${id}/deletion-impact`)
    return response.data
  }

  async deleteTopic(id: number, force: boolean = false): Promise<any> {
    const response = await api.delete(`${this.baseUrl}/topics/${id}?force=${force}`)
    return response.data
  }

  // ============ TOPIC NOTE OPERATIONS ============

  async getTopicNotes(params: PageRequest & { topicId?: number } = {}): Promise<PageResponse<TopicNoteDto>> {
    const searchParams = new URLSearchParams()
    if (params.page !== undefined) searchParams.append('page', params.page.toString())
    if (params.size !== undefined) searchParams.append('size', params.size.toString())
    if (params.sortBy) searchParams.append('sortBy', params.sortBy)
    if (params.sortDir) searchParams.append('sortDir', params.sortDir)
    if (params.search) searchParams.append('search', params.search)
    if (params.active !== undefined) searchParams.append('active', params.active.toString())
    if (params.topicId) searchParams.append('topicId', params.topicId.toString())
    
    const response = await api.get<PageResponse<TopicNoteDto>>(`${this.baseUrl}/topic-notes?${searchParams}`)
    return response.data
  }

  async createTopicNote(request: CreateTopicNoteRequest): Promise<TopicNoteDto> {
    const response = await api.post<TopicNoteDto>(`${this.baseUrl}/topic-notes`, request)
    return response.data
  }

  async updateTopicNote(id: number, request: UpdateTopicNoteRequest): Promise<TopicNoteDto> {
    const response = await api.put<TopicNoteDto>(`${this.baseUrl}/topic-notes/${id}`, request)
    return response.data
  }

  async updateTopicNoteStatus(id: number, active: boolean): Promise<TopicNoteDto> {
    const response = await api.patch<TopicNoteDto>(`${this.baseUrl}/topic-notes/${id}/status`, { active })
    return response.data
  }

  async deleteTopicNote(id: number, force: boolean = false): Promise<any> {
    const response = await api.delete(`${this.baseUrl}/topic-notes/${id}?force=${force}`)
    return response.data
  }

  // ============ UTILITY OPERATIONS ============

  async getBoardsDropdown(): Promise<BoardDropdownDto[]> {
    const response = await api.get<BoardDropdownDto[]>(`${this.utilsUrl}/boards/dropdown`)
    return response.data
  }

  async getGradesDropdown(boardId?: number): Promise<GradeDropdownDto[]> {
    const params = boardId ? `?boardId=${boardId}` : ''
    const response = await api.get<GradeDropdownDto[]>(`${this.utilsUrl}/grades/dropdown${params}`)
    return response.data
  }

  async getSubjectsDropdown(gradeId?: number): Promise<SubjectDropdownDto[]> {
    const params = gradeId ? `?gradeId=${gradeId}` : ''
    const response = await api.get<SubjectDropdownDto[]>(`${this.utilsUrl}/subjects/dropdown${params}`)
    return response.data
  }

  async getChaptersDropdown(subjectId?: number): Promise<ChapterDropdownDto[]> {
    const params = subjectId ? `?subjectId=${subjectId}` : ''
    const response = await api.get<ChapterDropdownDto[]>(`${this.utilsUrl}/chapters/dropdown${params}`)
    return response.data
  }

  async getTopicsDropdown(chapterId?: number): Promise<TopicDropdownDto[]> {
    const params = chapterId ? `?chapterId=${chapterId}` : ''
    const response = await api.get<TopicDropdownDto[]>(`${this.utilsUrl}/topics/dropdown${params}`)
    return response.data
  }

  async getContentTree(boardId?: number, gradeId?: number, subjectId?: number, chapterId?: number): Promise<ContentTreeDto> {
    const params = new URLSearchParams()
    if (boardId) params.append('boardId', boardId.toString())
    if (gradeId) params.append('gradeId', gradeId.toString())
    if (subjectId) params.append('subjectId', subjectId.toString())
    if (chapterId) params.append('chapterId', chapterId.toString())
    
    const queryString = params.toString()
    const url = `${this.utilsUrl}/content-tree${queryString ? `?${queryString}` : ''}`
    const response = await api.get<ContentTreeDto>(url)
    return response.data
  }
}

// Export singleton instance
export const adminContentService = new AdminContentService()
