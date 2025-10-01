/**
 * React Query hooks for Admin Content Management
 * Provides optimized data fetching, caching, and mutation handling
 */

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { 
  adminContentService,
  PageRequest,
  PageResponse,
  BoardDto,
  GradeDto,
  SubjectDto,
  ChapterDto,
  TopicDto,
  TopicNoteDto,
  CreateBoardRequest,
  UpdateBoardRequest,
  CreateGradeRequest,
  UpdateGradeRequest,
  CreateSubjectRequest,
  UpdateSubjectRequest,
  CreateChapterRequest,
  UpdateChapterRequest,
  CreateTopicRequest,
  UpdateTopicRequest,
  CreateTopicNoteRequest,
  UpdateTopicNoteRequest,
  BoardDropdownDto,
  GradeDropdownDto,
  SubjectDropdownDto,
  ChapterDropdownDto,
  TopicDropdownDto,
  ContentTreeDto,
  DeletionImpactDto
} from '@/services/adminContentService'

// =========================== QUERY KEYS ===========================

export const adminContentKeys = {
  all: ['adminContent'] as const,
  boards: () => [...adminContentKeys.all, 'boards'] as const,
  board: (id: number) => [...adminContentKeys.boards(), id] as const,
  boardsDropdown: () => [...adminContentKeys.boards(), 'dropdown'] as const,
  grades: () => [...adminContentKeys.all, 'grades'] as const,
  gradesByBoard: (boardId?: number) => [...adminContentKeys.grades(), 'byBoard', boardId] as const,
  gradesDropdown: (boardId?: number) => [...adminContentKeys.grades(), 'dropdown', boardId] as const,
  subjects: () => [...adminContentKeys.all, 'subjects'] as const,
  subjectsByGrade: (gradeId?: number) => [...adminContentKeys.subjects(), 'byGrade', gradeId] as const,
  subjectsDropdown: (gradeId?: number) => [...adminContentKeys.subjects(), 'dropdown', gradeId] as const,
  chapters: () => [...adminContentKeys.all, 'chapters'] as const,
  chaptersBySubject: (subjectId?: number) => [...adminContentKeys.chapters(), 'bySubject', subjectId] as const,
  chaptersDropdown: (subjectId?: number) => [...adminContentKeys.chapters(), 'dropdown', subjectId] as const,
  topics: () => [...adminContentKeys.all, 'topics'] as const,
  topicsByChapter: (chapterId?: number) => [...adminContentKeys.topics(), 'byChapter', chapterId] as const,
  topicsDropdown: (chapterId?: number) => [...adminContentKeys.topics(), 'dropdown', chapterId] as const,
  topicNotes: () => [...adminContentKeys.all, 'topicNotes'] as const,
  topicNotesByTopic: (topicId?: number) => [...adminContentKeys.topicNotes(), 'byTopic', topicId] as const,
  contentTree: (boardId?: number, gradeId?: number, subjectId?: number, chapterId?: number) => 
    [...adminContentKeys.all, 'contentTree', boardId, gradeId, subjectId, chapterId] as const,
  deletionImpact: (entityType: string, id: number) => 
    [...adminContentKeys.all, 'deletionImpact', entityType, id] as const,
}

// =========================== BOARDS ===========================

export function useBoards(params?: PageRequest) {
  return useQuery({
    queryKey: [...adminContentKeys.boards(), params],
    queryFn: async () => {
      console.log('useBoards: Starting query with params:', params)
      try {
        const result = await adminContentService.getBoards(params)
        console.log('useBoards: Query successful:', result)
        return result
      } catch (error) {
        console.error('useBoards: Query failed:', error)
        throw error
      }
    },
    staleTime: 30000, // 30 seconds
    retry: (failureCount, error) => {
      console.log('useBoards: Retry attempt', failureCount, 'for error:', error)
      return failureCount < 2 // Retry up to 2 times
    }
  })
}

export function useBoardsDropdown() {
  return useQuery({
    queryKey: adminContentKeys.boardsDropdown(),
    queryFn: () => adminContentService.getBoardsDropdown(),
    staleTime: 60000, // 1 minute - dropdown data changes less frequently
  })
}

export function useCreateBoard() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: (request: CreateBoardRequest) => adminContentService.createBoard(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.boards() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.boardsDropdown() })
      toast.success('Board created successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to create board: ${error.message}`)
    },
  })
}

export function useUpdateBoard() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: UpdateBoardRequest }) => 
      adminContentService.updateBoard(id, request),
    onSuccess: (data, { id }) => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.boards() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.boardsDropdown() })
      queryClient.setQueryData(adminContentKeys.board(id), data)
      toast.success('Board updated successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to update board: ${error.message}`)
    },
  })
}

export function useUpdateBoardStatus() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, active }: { id: number; active: boolean }) => 
      adminContentService.updateBoardStatus(id, active),
    onSuccess: (data, { id }) => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.boards() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.boardsDropdown() })
      queryClient.setQueryData(adminContentKeys.board(id), data)
      toast.success(`Board ${data.active ? 'activated' : 'deactivated'} successfully`)
    },
    onError: (error: Error) => {
      toast.error(`Failed to update board status: ${error.message}`)
    },
  })
}

export function useBoardDeletionImpact(id: number) {
  return useQuery({
    queryKey: adminContentKeys.deletionImpact('board', id),
    queryFn: () => adminContentService.getBoardDeletionImpact(id),
    enabled: !!id,
  })
}

export function useDeleteBoard() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, force }: { id: number; force?: boolean }) => 
      adminContentService.deleteBoard(id, force),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.boards() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.boardsDropdown() })
      toast.success('Board deleted successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to delete board: ${error.message}`)
    },
  })
}

// =========================== GRADES ===========================

export function useGrades(params?: PageRequest & { boardId?: number }) {
  return useQuery({
    queryKey: [...adminContentKeys.gradesByBoard(params?.boardId), params],
    queryFn: () => adminContentService.getGrades(params),
    staleTime: 30000,
  })
}

export function useGradesDropdown(boardId?: number) {
  return useQuery({
    queryKey: adminContentKeys.gradesDropdown(boardId),
    queryFn: () => adminContentService.getGradesDropdown(boardId),
    staleTime: 60000,
  })
}

export function useCreateGrade() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: (request: CreateGradeRequest) => adminContentService.createGrade(request),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.grades() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.gradesDropdown() })
      toast.success('Grade created successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to create grade: ${error.message}`)
    },
  })
}

export function useUpdateGrade() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: UpdateGradeRequest }) => 
      adminContentService.updateGrade(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.grades() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.gradesDropdown() })
      toast.success('Grade updated successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to update grade: ${error.message}`)
    },
  })
}

export function useUpdateGradeStatus() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, active }: { id: number; active: boolean }) => 
      adminContentService.updateGradeStatus(id, active),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.grades() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.gradesDropdown() })
      toast.success(`Grade ${data.active ? 'activated' : 'deactivated'} successfully`)
    },
    onError: (error: Error) => {
      toast.error(`Failed to update grade status: ${error.message}`)
    },
  })
}

export function useDeleteGrade() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, force }: { id: number; force?: boolean }) => 
      adminContentService.deleteGrade(id, force),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.grades() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.gradesDropdown() })
      toast.success('Grade deleted successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to delete grade: ${error.message}`)
    },
  })
}

// =========================== SUBJECTS ===========================

export function useSubjects(params?: PageRequest & { boardId?: number; gradeId?: number }) {
  return useQuery({
    queryKey: [...adminContentKeys.subjectsByGrade(params?.gradeId), params],
    queryFn: () => adminContentService.getSubjects(params),
    staleTime: 30000,
  })
}

export function useSubjectsDropdown(gradeId?: number) {
  return useQuery({
    queryKey: adminContentKeys.subjectsDropdown(gradeId),
    queryFn: () => adminContentService.getSubjectsDropdown(gradeId),
    staleTime: 60000,
  })
}

export function useCreateSubject() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: (request: CreateSubjectRequest) => adminContentService.createSubject(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.subjects() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.subjectsDropdown() })
      toast.success('Subject created successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to create subject: ${error.message}`)
    },
  })
}

export function useUpdateSubject() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: UpdateSubjectRequest }) => 
      adminContentService.updateSubject(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.subjects() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.subjectsDropdown() })
      toast.success('Subject updated successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to update subject: ${error.message}`)
    },
  })
}

export function useUpdateSubjectStatus() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, active }: { id: number; active: boolean }) => 
      adminContentService.updateSubjectStatus(id, active),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.subjects() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.subjectsDropdown() })
      toast.success(`Subject ${data.active ? 'activated' : 'deactivated'} successfully`)
    },
    onError: (error: Error) => {
      toast.error(`Failed to update subject status: ${error.message}`)
    },
  })
}

export function useSubjectDeletionImpact(id: number) {
  return useQuery({
    queryKey: adminContentKeys.deletionImpact('subject', id),
    queryFn: () => adminContentService.getSubjectDeletionImpact(id),
    enabled: !!id,
  })
}

export function useDeleteSubject() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, force }: { id: number; force?: boolean }) => 
      adminContentService.deleteSubject(id, force),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.subjects() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.subjectsDropdown() })
      toast.success('Subject deleted successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to delete subject: ${error.message}`)
    },
  })
}

// =========================== CHAPTERS ===========================

export function useChapters(params?: PageRequest & { subjectId?: number }) {
  return useQuery({
    queryKey: [...adminContentKeys.chaptersBySubject(params?.subjectId), params],
    queryFn: () => adminContentService.getChapters(params),
    staleTime: 30000,
  })
}

export function useChaptersDropdown(subjectId?: number) {
  return useQuery({
    queryKey: adminContentKeys.chaptersDropdown(subjectId),
    queryFn: () => adminContentService.getChaptersDropdown(subjectId),
    staleTime: 60000,
  })
}

export function useCreateChapter() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: (request: CreateChapterRequest) => adminContentService.createChapter(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.chapters() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.chaptersDropdown() })
      toast.success('Chapter created successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to create chapter: ${error.message}`)
    },
  })
}

export function useUpdateChapter() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: UpdateChapterRequest }) => 
      adminContentService.updateChapter(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.chapters() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.chaptersDropdown() })
      toast.success('Chapter updated successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to update chapter: ${error.message}`)
    },
  })
}

export function useUpdateChapterStatus() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, active }: { id: number; active: boolean }) => 
      adminContentService.updateChapterStatus(id, active),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.chapters() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.chaptersDropdown() })
      toast.success(`Chapter ${data.active ? 'activated' : 'deactivated'} successfully`)
    },
    onError: (error: Error) => {
      toast.error(`Failed to update chapter status: ${error.message}`)
    },
  })
}

export function useChapterDeletionImpact(id: number) {
  return useQuery({
    queryKey: adminContentKeys.deletionImpact('chapter', id),
    queryFn: () => adminContentService.getChapterDeletionImpact(id),
    enabled: !!id,
  })
}

export function useDeleteChapter() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, force }: { id: number; force?: boolean }) => 
      adminContentService.deleteChapter(id, force),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.chapters() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.chaptersDropdown() })
      toast.success('Chapter deleted successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to delete chapter: ${error.message}`)
    },
  })
}

// =========================== TOPICS ===========================

export function useTopics(params?: PageRequest & { chapterId?: number; subjectId?: number }) {
  return useQuery({
    queryKey: [...adminContentKeys.topicsByChapter(params?.chapterId), params],
    queryFn: () => adminContentService.getTopics(params),
    staleTime: 30000,
  })
}

export function useTopicsDropdown(chapterId?: number) {
  return useQuery({
    queryKey: adminContentKeys.topicsDropdown(chapterId),
    queryFn: () => adminContentService.getTopicsDropdown(chapterId),
    staleTime: 60000,
  })
}

export function useCreateTopic() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: (request: CreateTopicRequest) => adminContentService.createTopic(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.topics() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.topicsDropdown() })
      toast.success('Topic created successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to create topic: ${error.message}`)
    },
  })
}

export function useUpdateTopic() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: UpdateTopicRequest }) => 
      adminContentService.updateTopic(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.topics() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.topicsDropdown() })
      toast.success('Topic updated successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to update topic: ${error.message}`)
    },
  })
}

export function useUpdateTopicStatus() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, active }: { id: number; active: boolean }) => 
      adminContentService.updateTopicStatus(id, active),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.topics() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.topicsDropdown() })
      toast.success(`Topic ${data.active ? 'activated' : 'deactivated'} successfully`)
    },
    onError: (error: Error) => {
      toast.error(`Failed to update topic status: ${error.message}`)
    },
  })
}

export function useTopicDeletionImpact(id: number) {
  return useQuery({
    queryKey: adminContentKeys.deletionImpact('topic', id),
    queryFn: () => adminContentService.getTopicDeletionImpact(id),
    enabled: !!id,
  })
}

export function useDeleteTopic() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, force }: { id: number; force?: boolean }) => 
      adminContentService.deleteTopic(id, force),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.topics() })
      queryClient.invalidateQueries({ queryKey: adminContentKeys.topicsDropdown() })
      toast.success('Topic deleted successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to delete topic: ${error.message}`)
    },
  })
}

// =========================== TOPIC NOTES ===========================

export function useTopicNotes(params?: PageRequest & { topicId?: number }) {
  return useQuery({
    queryKey: [...adminContentKeys.topicNotesByTopic(params?.topicId), params],
    queryFn: () => adminContentService.getTopicNotes(params),
    staleTime: 30000,
  })
}

export function useCreateTopicNote() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: (request: CreateTopicNoteRequest) => adminContentService.createTopicNote(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.topicNotes() })
      toast.success('Topic note created successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to create topic note: ${error.message}`)
    },
  })
}

export function useUpdateTopicNote() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, request }: { id: number; request: UpdateTopicNoteRequest }) => 
      adminContentService.updateTopicNote(id, request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.topicNotes() })
      toast.success('Topic note updated successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to update topic note: ${error.message}`)
    },
  })
}

export function useUpdateTopicNoteStatus() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, active }: { id: number; active: boolean }) => 
      adminContentService.updateTopicNoteStatus(id, active),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.topicNotes() })
      toast.success(`Topic note ${data.active ? 'activated' : 'deactivated'} successfully`)
    },
    onError: (error: Error) => {
      toast.error(`Failed to update topic note status: ${error.message}`)
    },
  })
}

export function useDeleteTopicNote() {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: ({ id, force }: { id: number; force?: boolean }) => 
      adminContentService.deleteTopicNote(id, force),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: adminContentKeys.topicNotes() })
      toast.success('Topic note deleted successfully')
    },
    onError: (error: Error) => {
      toast.error(`Failed to delete topic note: ${error.message}`)
    },
  })
}

// =========================== UTILITY HOOKS ===========================

export function useContentTree(boardId?: number, gradeId?: number, subjectId?: number, chapterId?: number) {
  return useQuery({
    queryKey: adminContentKeys.contentTree(boardId, gradeId, subjectId, chapterId),
    queryFn: () => adminContentService.getContentTree(boardId, gradeId, subjectId, chapterId),
    staleTime: 60000,
  })
}
