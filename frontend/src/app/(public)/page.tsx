import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export default function PublicLandingPage() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* Header */}
      <header className="border-b bg-white/80 backdrop-blur-sm">
        <div className="container mx-auto px-4 py-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <div className="h-8 w-8 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-lg"></div>
              <span className="text-xl font-bold text-gray-900">Ankurshala</span>
            </div>
            <div className="flex items-center space-x-4">
              <Link href="/login">
                <Button variant="ghost">Login</Button>
              </Link>
              <Link href="/register-student">
                <Button>Get Started</Button>
              </Link>
            </div>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="py-20">
        <div className="container mx-auto px-4 text-center">
          <h1 className="text-5xl font-bold text-gray-900 mb-6">
            Learn from the Best Teachers
          </h1>
          <p className="text-xl text-gray-600 mb-8 max-w-2xl mx-auto">
            Connect with expert teachers for personalized 1-on-1 classes. 
            Book sessions, track progress, and achieve your learning goals.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link href="/register-student">
              <Button size="lg" className="bg-blue-600 hover:bg-blue-700">
                Start Learning as Student
              </Button>
            </Link>
            <Link href="/register-teacher">
              <Button size="lg" variant="outline" className="border-blue-600 text-blue-600 hover:bg-blue-50">
                Teach as Expert
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-16 bg-white">
        <div className="container mx-auto px-4">
          <h2 className="text-3xl font-bold text-center text-gray-900 mb-12">
            Why Choose Ankurshala?
          </h2>
          <div className="grid md:grid-cols-3 gap-8">
            <Card className="text-center">
              <CardHeader>
                <div className="h-12 w-12 bg-green-100 rounded-lg mx-auto mb-4 flex items-center justify-center">
                  <div className="h-6 w-6 bg-green-600 rounded"></div>
                </div>
                <CardTitle>Expert Teachers</CardTitle>
                <CardDescription>
                  Learn from verified teachers with proven expertise in their subjects
                </CardDescription>
              </CardHeader>
            </Card>

            <Card className="text-center">
              <CardHeader>
                <div className="h-12 w-12 bg-blue-100 rounded-lg mx-auto mb-4 flex items-center justify-center">
                  <div className="h-6 w-6 bg-blue-600 rounded"></div>
                </div>
                <CardTitle>Flexible Scheduling</CardTitle>
                <CardDescription>
                  Book classes at your convenience with our easy scheduling system
                </CardDescription>
              </CardHeader>
            </Card>

            <Card className="text-center">
              <CardHeader>
                <div className="h-12 w-12 bg-purple-100 rounded-lg mx-auto mb-4 flex items-center justify-center">
                  <div className="h-6 w-6 bg-purple-600 rounded"></div>
                </div>
                <CardTitle>Track Progress</CardTitle>
                <CardDescription>
                  Monitor your learning journey with detailed progress tracking
                </CardDescription>
              </CardHeader>
            </Card>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-gradient-to-r from-blue-600 to-indigo-600">
        <div className="container mx-auto px-4 text-center">
          <h2 className="text-3xl font-bold text-white mb-4">
            Ready to Start Learning?
          </h2>
          <p className="text-xl text-blue-100 mb-8">
            Join thousands of students already learning with Ankurshala
          </p>
          <Link href="/register-student">
            <Button size="lg" variant="secondary" className="bg-white text-blue-600 hover:bg-gray-50">
              Get Started Now
            </Button>
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-white py-12">
        <div className="container mx-auto px-4">
          <div className="grid md:grid-cols-4 gap-8">
            <div>
              <div className="flex items-center space-x-2 mb-4">
                <div className="h-8 w-8 bg-gradient-to-r from-blue-600 to-indigo-600 rounded-lg"></div>
                <span className="text-xl font-bold">Ankurshala</span>
              </div>
              <p className="text-gray-400">
                Connecting students with expert teachers for personalized learning experiences.
              </p>
            </div>
            <div>
              <h3 className="font-semibold mb-4">For Students</h3>
              <ul className="space-y-2 text-gray-400">
                <li><Link href="/register-student" className="hover:text-white">Sign Up</Link></li>
                <li><Link href="/login" className="hover:text-white">Login</Link></li>
                <li><Link href="/student/dashboard" className="hover:text-white">Dashboard</Link></li>
              </ul>
            </div>
            <div>
              <h3 className="font-semibold mb-4">For Teachers</h3>
              <ul className="space-y-2 text-gray-400">
                <li><Link href="/register-teacher" className="hover:text-white">Teach with Us</Link></li>
                <li><Link href="/teacher/dashboard" className="hover:text-white">Teacher Dashboard</Link></li>
              </ul>
            </div>
            <div>
              <h3 className="font-semibold mb-4">Support</h3>
              <ul className="space-y-2 text-gray-400">
                <li><Link href="#" className="hover:text-white">Help Center</Link></li>
                <li><Link href="#" className="hover:text-white">Contact Us</Link></li>
                <li><Link href="#" className="hover:text-white">Privacy Policy</Link></li>
              </ul>
            </div>
          </div>
          <div className="border-t border-gray-800 mt-8 pt-8 text-center text-gray-400">
            <p>&copy; 2024 Ankurshala. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
