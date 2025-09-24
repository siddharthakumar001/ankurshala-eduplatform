'use client'

import AdminLayoutSimple from '@/components/admin-layout-simple'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { BookOpen, Search, Filter, ToggleLeft, ToggleRight } from 'lucide-react'

export default function AdminContentBrowsePage() {
  return (
    <AdminLayoutSimple>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Browse Content</h1>
            <p className="text-gray-600 dark:text-gray-400">Manage educational content hierarchy</p>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Panel - Content Hierarchy */}
          <Card className="p-6">
            <div className="space-y-4">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Content Hierarchy</h3>
              
              {/* Search */}
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
                <input
                  type="text"
                  placeholder="Search content..."
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent dark:bg-gray-700 dark:text-white"
                />
              </div>

              {/* Filters */}
              <div className="space-y-2">
                <Button variant="outline" className="w-full justify-start">
                  <Filter className="h-4 w-4 mr-2" />
                  Board: CBSE
                </Button>
                <Button variant="outline" className="w-full justify-start">
                  <Filter className="h-4 w-4 mr-2" />
                  Grade: 8
                </Button>
                <Button variant="outline" className="w-full justify-start">
                  <Filter className="h-4 w-4 mr-2" />
                  Subject: Mathematics
                </Button>
              </div>

              {/* Hierarchy Tree */}
              <div className="space-y-2">
                <div className="text-sm font-medium text-gray-700 dark:text-gray-300">Boards</div>
                <div className="ml-4 space-y-1">
                  <div className="flex items-center justify-between py-1">
                    <span className="text-sm text-gray-600 dark:text-gray-400">CBSE</span>
                    <ToggleRight className="h-4 w-4 text-green-500" />
                  </div>
                  <div className="flex items-center justify-between py-1">
                    <span className="text-sm text-gray-600 dark:text-gray-400">ICSE</span>
                    <ToggleRight className="h-4 w-4 text-green-500" />
                  </div>
                </div>
                
                <div className="text-sm font-medium text-gray-700 dark:text-gray-300 mt-4">Grades</div>
                <div className="ml-4 space-y-1">
                  <div className="flex items-center justify-between py-1">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Grade 6</span>
                    <ToggleRight className="h-4 w-4 text-green-500" />
                  </div>
                  <div className="flex items-center justify-between py-1">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Grade 7</span>
                    <ToggleRight className="h-4 w-4 text-green-500" />
                  </div>
                  <div className="flex items-center justify-between py-1">
                    <span className="text-sm text-gray-600 dark:text-gray-400">Grade 8</span>
                    <ToggleLeft className="h-4 w-4 text-gray-400" />
                  </div>
                </div>
              </div>
            </div>
          </Card>

          {/* Right Panel - Topics */}
          <div className="lg:col-span-2">
            <Card className="p-6">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Topics</h3>
                  <div className="flex space-x-2">
                    <Button variant="outline" size="sm">Bulk Activate</Button>
                    <Button variant="outline" size="sm">Bulk Deactivate</Button>
                  </div>
                </div>

                {/* Topics Grid */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {[...Array(6)].map((_, i) => (
                    <div key={i} className="border border-gray-200 dark:border-gray-700 rounded-lg p-4 hover:shadow-md transition-shadow">
                      <div className="flex items-start justify-between mb-3">
                        <div className="flex items-center">
                          <BookOpen className="h-5 w-5 text-blue-500 mr-2" />
                          <h4 className="font-medium text-gray-900 dark:text-white">
                            Topic {i + 1}
                          </h4>
                        </div>
                        <div className="flex items-center">
                          {i % 2 === 0 ? (
                            <ToggleRight className="h-5 w-5 text-green-500" />
                          ) : (
                            <ToggleLeft className="h-5 w-5 text-gray-400" />
                          )}
                        </div>
                      </div>
                      
                      <p className="text-sm text-gray-600 dark:text-gray-400 mb-3">
                        This is a sample topic description that explains the content and learning objectives.
                      </p>
                      
                      <div className="flex items-center justify-between text-xs text-gray-500 dark:text-gray-400">
                        <span>Expected Time: {30 + i * 5} mins</span>
                        <span>Chapter {Math.floor(i / 2) + 1}</span>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Pagination */}
                <div className="flex items-center justify-between">
                  <div className="text-sm text-gray-500 dark:text-gray-400">
                    Showing 1-6 of 24 topics
                  </div>
                  <div className="flex space-x-2">
                    <Button variant="outline" size="sm" disabled>Previous</Button>
                    <Button variant="outline" size="sm">Next</Button>
                  </div>
                </div>
              </div>
            </Card>
          </div>
        </div>
      </div>
    </AdminLayoutSimple>
  )
}
