import Link from 'next/link'
import Image from 'next/image'

// Stage-1 FE complete: Forbidden page for role mismatch
export default function ForbiddenPage() {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center px-4">
      <div className="text-center">
        <div className="mb-8">
          <Image
            src="/ankurshala.svg"
            alt="Ankurshala Logo"
            width={80}
            height={80}
            className="mx-auto rounded-lg"
          />
        </div>
        
        <h1 className="text-6xl font-bold text-red-600 mb-4">403</h1>
        <h2 className="text-2xl font-semibold text-gray-700 mb-4">Access Forbidden</h2>
        <p className="text-gray-600 mb-8 max-w-md">
          You don't have permission to access this page. Please check your account permissions or contact support if you believe this is an error.
        </p>
        
        <div className="space-x-4">
          <Link
            href="/"
            className="inline-flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            Go Home
          </Link>
          <Link
            href="/login"
            className="inline-flex items-center px-6 py-3 border border-gray-300 text-base font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
          >
            Sign In
          </Link>
        </div>
        
        <div className="mt-8 text-sm text-gray-500">
          <p>Need help? Contact us at support@ankurshala.com</p>
        </div>
      </div>
    </div>
  )
}
