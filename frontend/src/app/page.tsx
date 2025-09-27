import Image from 'next/image'
import Link from 'next/link'
import { Button } from '@/components/ui/button'
import { Sparkles, CalendarDays, ShieldCheck, Brain } from 'lucide-react'

export const dynamic = 'force-static'

export default function Home() {
  return (
    <main className="min-h-screen bg-gradient-to-b from-[#0B1220] via-[#0E1730] to-[#0B1220] text-white">
      {/* HERO */}
      <section className="relative container mx-auto px-6 pt-16 pb-10 md:pt-24 md:pb-16">
        {/* Soft glow */}
        <div className="pointer-events-none absolute -top-24 left-1/2 -translate-x-1/2 h-72 w-72 rounded-full blur-3xl opacity-40"
             style={{ background: 'radial-gradient(60% 60% at 50% 50%, #10B98155, #1E3A8A00)' }} />
        <div className="flex flex-col items-center text-center">
          <Image
            src="/ankurshala.svg"
            alt="Ankurshala"
            width={200}
            height={200}
            priority
            className="drop-shadow-[0_10px_20px_rgba(16,185,129,0.35)]"
          />
          <h1 className="mt-8 text-4xl md:text-6xl font-extrabold tracking-tight">
            Smart Learning, On&nbsp;Demand
          </h1>
          <p className="mt-4 max-w-2xl text-lg md:text-xl text-slate-300">
            Book 1:1 sessions by topic. Get matched with verified teachers in minutes.
            AI guides your prep with personalized recommendations and summaries.
          </p>

          {/* Primary CTAs (no top nav) */}
          <div className="mt-8 flex flex-col sm:flex-row gap-3">
            <Button size="lg" className="bg-emerald-500 hover:bg-emerald-600 text-white" asChild>
              <Link href="/login">
                <Sparkles className="mr-2 h-5 w-5" /> Login
              </Link>
            </Button>
            <Button size="lg" variant="outline" className="border-emerald-400 text-emerald-300 hover:bg-emerald-500/10" asChild>
              <Link href="/register-student">I'm a Student</Link>
            </Button>
            <Button size="lg" variant="outline" className="border-blue-400 text-blue-300 hover:bg-blue-500/10" asChild>
              <Link href="/register-teacher">I'm a Teacher</Link>
            </Button>
          </div>

          {/* Quick dock (replaces navbar) */}
          <div className="mt-10 flex flex-wrap justify-center gap-3">
            {[
              { href: '/how-it-works', label: 'How it works' },
              { href: '/subjects', label: 'Subjects & Boards' },
              { href: '/safety', label: 'Safety & Quality' },
            ].map((item) => (
              <Link key={item.href} href={item.href}
                className="rounded-full border border-white/10 bg-white/5 px-4 py-2 text-sm text-slate-200 hover:bg-white/10">
                {item.label}
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* BENEFITS */}
      <section className="container mx-auto px-6 py-12 md:py-16">
        <div className="grid gap-6 md:grid-cols-3">
          <div className="rounded-xl border border-white/10 bg-white/5 p-6">
            <CalendarDays className="h-6 w-6 text-emerald-400" />
            <h3 className="mt-3 text-xl font-semibold">Book by Topic & Time</h3>
            <p className="mt-2 text-slate-300">
              Pick a subject, choose a topic, select your slot. Teachers accept in real time—Uber-style.
            </p>
          </div>
          <div className="rounded-xl border border-white/10 bg-white/5 p-6">
            <ShieldCheck className="h-6 w-6 text-blue-400" />
            <h3 className="mt-3 text-xl font-semibold">Verified Teachers</h3>
            <p className="mt-2 text-slate-300">
              Background-checked experts across CBSE, ICSE, IB and Cambridge. Quality you can trust.
            </p>
          </div>
          <div className="rounded-xl border border-white/10 bg-white/5 p-6">
            <Brain className="h-6 w-6 text-yellow-300" />
            <h3 className="mt-3 text-xl font-semibold">AI-Enabled Learning</h3>
            <p className="mt-2 text-slate-300">
              Personalized prep lists, weak-area insights, and after-class summaries—powered by AI.
            </p>
          </div>
        </div>
      </section>

      {/* SUBJECTS */}
      <section className="container mx-auto px-6 pb-12 md:pb-16">
        <h2 className="text-2xl md:text-3xl font-bold mb-6">Popular Subjects</h2>
        <div className="grid gap-3 sm:grid-cols-2 md:grid-cols-4">
          {['Mathematics','Physics','Chemistry','Biology','English','Computer Science','Economics','Social Science']
            .map((s) => (
              <span key={s}
                className="rounded-lg border border-white/10 bg-white/5 px-4 py-3 text-center text-slate-200 hover:bg-white/10">
                {s}
              </span>
            ))}
        </div>
      </section>

      {/* CTA STRIP */}
      <section className="border-t border-white/10 bg-gradient-to-r from-emerald-600/20 to-blue-600/20">
        <div className="container mx-auto px-6 py-10 text-center">
          <h3 className="text-2xl md:text-3xl font-semibold">
            Ready to start? Join Ankurshala today.
          </h3>
          <p className="mt-2 text-slate-300">
            Students learn faster with focused 1:1 sessions. Teachers grow with flexible, meaningful work.
          </p>
                 <div className="mt-6 flex flex-col sm:flex-row gap-3 justify-center">
                   <Button size="lg" className="bg-emerald-500 hover:bg-emerald-600 text-white" asChild>
                     <Link href="/register-student">Get Started (Student)</Link>
                   </Button>
                   <Button size="lg" variant="outline" className="border-blue-400 text-blue-300 hover:bg-blue-500/10" asChild>
                     <Link href="/register-teacher">Become a Teacher</Link>
                   </Button>
                 </div>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="border-t border-white/10">
        <div className="container mx-auto px-6 py-8 flex flex-col items-center gap-3">
          <Image src="/ankurshala.svg" width={60} height={60} alt="Ankurshala" />
          <p className="text-slate-400 text-sm">
            © {new Date().getFullYear()} Ankurshala • On Demand Learning
          </p>
        </div>
      </footer>

      {/* Floating role actions (mobile-friendly) */}
      <div className="fixed inset-x-0 bottom-4 flex justify-center px-4 md:hidden">
        <div className="flex gap-2 rounded-full border border-white/10 bg-[#0E1730]/80 backdrop-blur px-3 py-2">
          <Link href="/login" className="rounded-full bg-emerald-500 px-4 py-2 text-sm">Login</Link>
          <Link href="/register-student" className="rounded-full px-4 py-2 text-sm border border-white/10">Student</Link>
          <Link href="/register-teacher" className="rounded-full px-4 py-2 text-sm border border-white/10">Teacher</Link>
        </div>
      </div>
    </main>
  )
}
