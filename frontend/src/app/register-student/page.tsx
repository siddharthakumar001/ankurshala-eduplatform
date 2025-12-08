'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import Image from 'next/image';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { useAuthStore } from '@/store/auth';
import { Loader2, ArrowLeft, ArrowRight, Check, GraduationCap, BookOpen, Users, Target, Star, Shield } from 'lucide-react';

const EDUCATIONAL_BOARDS = [
  { value: 'CBSE', label: 'CBSE - Central Board of Secondary Education' },
  { value: 'ICSE', label: 'ICSE - Indian Certificate of Secondary Education' },
  { value: 'IB', label: 'IB - International Baccalaureate' },
  { value: 'CAMBRIDGE', label: 'Cambridge International' },
  { value: 'STATE_BOARD', label: 'State Board' },
  { value: 'NIOS', label: 'NIOS - National Institute of Open Schooling' },
];

const CLASS_LEVELS = [
  { value: 'GRADE_1', label: 'Class 1' },
  { value: 'GRADE_2', label: 'Class 2' },
  { value: 'GRADE_3', label: 'Class 3' },
  { value: 'GRADE_4', label: 'Class 4' },
  { value: 'GRADE_5', label: 'Class 5' },
  { value: 'GRADE_6', label: 'Class 6' },
  { value: 'GRADE_7', label: 'Class 7' },
  { value: 'GRADE_8', label: 'Class 8' },
  { value: 'GRADE_9', label: 'Class 9' },
  { value: 'GRADE_10', label: 'Class 10' },
  { value: 'GRADE_11', label: 'Class 11' },
  { value: 'GRADE_12', label: 'Class 12' },
];

const learningGoals = [
  'Improve grades',
  'Exam preparation',
  'Concept clarity',
  'Homework help',
  'Competition preparation',
  'Advanced learning',
];

const steps = [
  { id: 1, title: 'Account', description: 'Your credentials' },
  { id: 2, title: 'Academic', description: 'School details' },
  { id: 3, title: 'Guardian', description: 'Parent info' },
  { id: 4, title: 'Goals', description: 'Learning goals' },
];

const studentSignupSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters'),
  email: z.string().email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  confirmPassword: z.string().min(6, 'Password must be at least 6 characters'),
  board: z.string().min(1, 'Please select your educational board'),
  grade: z.string().min(1, 'Please select your grade'),
  language: z.string().min(1, 'Please select your preferred language'),
  school: z.string().min(2, 'School name must be at least 2 characters'),
  dob: z.string().min(1, 'Please enter your date of birth'),
  pincode: z.string().min(6, 'Please enter a valid pincode'),
  guardianName: z.string().min(2, 'Guardian name must be at least 2 characters'),
  guardianContact: z.string().min(10, 'Please enter a valid contact number'),
  goals: z.array(z.string()).min(1, 'Please select at least one learning goal'),
  acceptTerms: z.boolean().refine(val => val === true, { message: 'You must accept the terms' }),
});

type StudentSignupForm = z.infer<typeof studentSignupSchema>;

export default function RegisterStudentPage() {
  const [currentStep, setCurrentStep] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();
  const { signup } = useAuthStore();

  const {
    register,
    handleSubmit,
    formState: { errors },
    setValue,
    watch,
    trigger,
  } = useForm<StudentSignupForm>({
    resolver: zodResolver(studentSignupSchema),
    mode: 'onChange',
    defaultValues: {
      goals: [],
      acceptTerms: false,
      board: '',
      grade: '',
      language: '',
    },
  });

  const watchedGoals = watch('goals') ?? [];
  const watchedAcceptTerms = watch('acceptTerms') ?? false;
  const watchedBoard = watch('board') ?? '';
  const watchedGrade = watch('grade') ?? '';
  const watchedLanguage = watch('language') ?? '';

  const handleGoalChange = (goal: string, checked: boolean) => {
    const currentGoals = watchedGoals;
    if (checked) {
      setValue('goals', [...currentGoals, goal]);
    } else {
      setValue('goals', currentGoals.filter(g => g !== goal));
    }
  };

  const nextStep = async () => {
    const fieldsToValidate = getFieldsForStep(currentStep);
    const isValid = await trigger(fieldsToValidate);
    if (isValid) {
      setCurrentStep(prev => Math.min(prev + 1, steps.length));
      setError(null);
    }
  };

  const prevStep = () => {
    setCurrentStep(prev => Math.max(prev - 1, 1));
    setError(null);
  };

  const getFieldsForStep = (step: number): (keyof StudentSignupForm)[] => {
    switch (step) {
      case 1: return ['name', 'email', 'password', 'confirmPassword'];
      case 2: return ['board', 'grade', 'language', 'school', 'dob', 'pincode'];
      case 3: return ['guardianName', 'guardianContact'];
      case 4: return ['goals', 'acceptTerms'];
      default: return [];
    }
  };

  const onSubmit = async (data: StudentSignupForm) => {
    if (data.password !== data.confirmPassword) {
      setError('Passwords do not match');
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const gradeValue = data.grade.replace('GRADE_', '');
      await signup('student', {
        name: data.name,
        email: data.email,
        password: data.password,
        board: data.board,
        grade: gradeValue,
        language: data.language,
        school: data.school,
        dob: data.dob,
        pincode: data.pincode,
        guardianName: data.guardianName,
        guardianContact: data.guardianContact,
        goals: data.goals,
      });
      router.push('/student/dashboard');
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Signup failed. Please try again.';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const features = [
    { icon: GraduationCap, text: 'Access to 500+ verified teachers' },
    { icon: BookOpen, text: 'All major boards supported' },
    { icon: Target, text: 'Personalized learning paths' },
    { icon: Star, text: 'AI-powered study recommendations' },
  ];

  return (
    <div className="min-h-screen flex">
      {/* Left Brand Section */}
      <div className="hidden lg:flex lg:w-5/12 bg-gradient-to-br from-ankur-secondary via-ankur-secondary to-ankur-primary/80 p-12 flex-col justify-between relative overflow-hidden">
        <div className="absolute top-0 right-0 w-96 h-96 bg-ankur-primary/20 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2" />
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-ankur-accent/20 rounded-full blur-3xl translate-y-1/2 -translate-x-1/2" />
        
        <div className="relative z-10">
          <Link href="/" className="flex items-center gap-3 mb-12">
            <Image src="/ankurshala-logo-small.png" width={56} height={56} alt="Ankurshala" className="rounded-lg" />
            <span className="text-2xl font-bold text-white">Ankurshala</span>
          </Link>
          
          <h1 className="text-4xl font-bold text-white mb-4">Start Your Learning Journey</h1>
          <p className="text-white/80 text-lg mb-12">
            Join thousands of students learning with expert teachers on India&apos;s most trusted learning platform.
          </p>
          
          <div className="space-y-6">
            {features.map((feature, index) => (
              <div key={index} className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-xl bg-white/10 backdrop-blur flex items-center justify-center">
                  <feature.icon className="h-6 w-6 text-ankur-accent" />
                </div>
                <span className="text-white/90 text-lg">{feature.text}</span>
              </div>
            ))}
          </div>
        </div>
        
        <div className="relative z-10 flex items-center gap-3 bg-white/10 backdrop-blur rounded-2xl p-4">
          <Shield className="h-10 w-10 text-ankur-accent" />
          <div>
            <p className="text-white font-semibold">100% Safe & Secure</p>
            <p className="text-white/70 text-sm">Your data is protected with enterprise-grade security</p>
          </div>
        </div>
      </div>

      {/* Right Form Section */}
      <div className="flex-1 flex items-center justify-center p-6 lg:p-12 bg-gray-50">
        <div className="w-full max-w-xl">
          <div className="lg:hidden mb-8 text-center">
            <Link href="/" className="inline-flex items-center gap-2 mb-4">
              <Image src="/ankurshala-logo-small.png" width={40} height={40} alt="Ankurshala" className="rounded-lg" />
              <span className="text-xl font-bold text-ankur-secondary">Ankurshala</span>
            </Link>
          </div>

          <Card className="border-0 shadow-xl bg-white">
            <CardContent className="p-8">
              <div className="text-center mb-8">
                <div className="inline-flex items-center justify-center w-14 h-14 rounded-xl bg-ankur-primary/10 mb-4">
                  <GraduationCap className="h-7 w-7 text-ankur-primary" />
                </div>
                <h2 className="text-2xl font-bold text-ankur-secondary">Student Registration</h2>
                <p className="text-gray-600 mt-1">Create your account in 4 easy steps</p>
              </div>

              {/* Progress Steps */}
              <div className="mb-8">
                <div className="flex items-center justify-between">
                  {steps.map((step, index) => (
                    <div key={step.id} className="flex items-center flex-1">
                      <div className="flex flex-col items-center flex-1">
                        <div className={`flex items-center justify-center w-10 h-10 rounded-full transition-all ${
                          currentStep >= step.id ? 'bg-ankur-primary text-white' : 'bg-gray-100 text-gray-400'
                        }`}>
                          {currentStep > step.id ? <Check className="h-5 w-5" /> : <span className="text-sm font-semibold">{step.id}</span>}
                        </div>
                        <div className="mt-2 text-center hidden sm:block">
                          <p className={`text-xs font-medium ${currentStep >= step.id ? 'text-ankur-primary' : 'text-gray-400'}`}>{step.title}</p>
                        </div>
                      </div>
                      {index < steps.length - 1 && (
                        <div className={`h-1 flex-1 mx-2 rounded ${currentStep > step.id ? 'bg-ankur-primary' : 'bg-gray-100'}`} />
                      )}
                    </div>
                  ))}
                </div>
              </div>

              {error && (
                <Alert className="mb-6 bg-red-50 border-red-200" variant="destructive">
                  <AlertDescription className="text-red-700">{error}</AlertDescription>
                </Alert>
              )}

              <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
                {/* Step 1: Account */}
                {currentStep === 1 && (
                  <div className="space-y-4">
                    <div>
                      <Label htmlFor="name" className="text-gray-700">Full Name</Label>
                      <Input id="name" {...register('name')} placeholder="Enter your full name" className={`mt-1.5 h-11 ${errors.name ? 'border-red-500' : 'border-gray-200'}`} />
                      {errors.name && <p className="text-sm text-red-500 mt-1">{errors.name.message}</p>}
                    </div>
                    <div>
                      <Label htmlFor="email" className="text-gray-700">Email Address</Label>
                      <Input id="email" type="email" {...register('email')} placeholder="Enter your email" className={`mt-1.5 h-11 ${errors.email ? 'border-red-500' : 'border-gray-200'}`} />
                      {errors.email && <p className="text-sm text-red-500 mt-1">{errors.email.message}</p>}
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label htmlFor="password" className="text-gray-700">Password</Label>
                        <Input id="password" type="password" {...register('password')} placeholder="Create password" className={`mt-1.5 h-11 ${errors.password ? 'border-red-500' : 'border-gray-200'}`} />
                        {errors.password && <p className="text-sm text-red-500 mt-1">{errors.password.message}</p>}
                      </div>
                      <div>
                        <Label htmlFor="confirmPassword" className="text-gray-700">Confirm</Label>
                        <Input id="confirmPassword" type="password" {...register('confirmPassword')} placeholder="Confirm password" className={`mt-1.5 h-11 ${errors.confirmPassword ? 'border-red-500' : 'border-gray-200'}`} />
                        {errors.confirmPassword && <p className="text-sm text-red-500 mt-1">{errors.confirmPassword.message}</p>}
                      </div>
                    </div>
                  </div>
                )}

                {/* Step 2: Academic */}
                {currentStep === 2 && (
                  <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label className="text-gray-700">Educational Board</Label>
                        <Select 
                          value={watchedBoard || ''} 
                          onValueChange={(value) => { 
                            setValue('board', value, { shouldValidate: true }); 
                            setValue('grade', '', { shouldValidate: false }); 
                          }}
                        >
                          <SelectTrigger className={`mt-1.5 h-11 ${errors.board ? 'border-red-500' : 'border-gray-200'}`}>
                            <SelectValue placeholder="Select board" />
                          </SelectTrigger>
                          <SelectContent>
                            {EDUCATIONAL_BOARDS.map((board) => <SelectItem key={board.value} value={board.value}>{board.value}</SelectItem>)}
                          </SelectContent>
                        </Select>
                        {errors.board && <p className="text-sm text-red-500 mt-1">{errors.board.message}</p>}
                      </div>
                      <div>
                        <Label className="text-gray-700">Grade/Class</Label>
                        <Select 
                          value={watchedGrade || ''} 
                          onValueChange={(value) => setValue('grade', value, { shouldValidate: true })} 
                          disabled={!watchedBoard}
                        >
                          <SelectTrigger className={`mt-1.5 h-11 ${errors.grade ? 'border-red-500' : 'border-gray-200'}`}>
                            <SelectValue placeholder={watchedBoard ? "Select grade" : "Select board first"} />
                          </SelectTrigger>
                          <SelectContent>
                            {CLASS_LEVELS.map((grade) => <SelectItem key={grade.value} value={grade.value}>{grade.label}</SelectItem>)}
                          </SelectContent>
                        </Select>
                        {errors.grade && <p className="text-sm text-red-500 mt-1">{errors.grade.message}</p>}
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label className="text-gray-700">Preferred Language</Label>
                        <Select 
                          value={watchedLanguage || ''} 
                          onValueChange={(value) => setValue('language', value, { shouldValidate: true })}
                        >
                          <SelectTrigger className={`mt-1.5 h-11 ${errors.language ? 'border-red-500' : 'border-gray-200'}`}>
                            <SelectValue placeholder="Select language" />
                          </SelectTrigger>
                          <SelectContent>
                            {['English', 'Hindi', 'Bengali', 'Tamil', 'Telugu', 'Marathi'].map((lang) => <SelectItem key={lang} value={lang}>{lang}</SelectItem>)}
                          </SelectContent>
                        </Select>
                        {errors.language && <p className="text-sm text-red-500 mt-1">{errors.language.message}</p>}
                      </div>
                      <div>
                        <Label htmlFor="dob" className="text-gray-700">Date of Birth</Label>
                        <Input id="dob" type="date" {...register('dob')} className={`mt-1.5 h-11 ${errors.dob ? 'border-red-500' : 'border-gray-200'}`} />
                        {errors.dob && <p className="text-sm text-red-500 mt-1">{errors.dob.message}</p>}
                      </div>
                    </div>
                    <div>
                      <Label htmlFor="school" className="text-gray-700">School Name</Label>
                      <Input id="school" {...register('school')} placeholder="Enter your school name" className={`mt-1.5 h-11 ${errors.school ? 'border-red-500' : 'border-gray-200'}`} />
                      {errors.school && <p className="text-sm text-red-500 mt-1">{errors.school.message}</p>}
                    </div>
                    <div>
                      <Label htmlFor="pincode" className="text-gray-700">Pincode</Label>
                      <Input id="pincode" {...register('pincode')} placeholder="Enter your pincode" className={`mt-1.5 h-11 ${errors.pincode ? 'border-red-500' : 'border-gray-200'}`} />
                      {errors.pincode && <p className="text-sm text-red-500 mt-1">{errors.pincode.message}</p>}
                    </div>
                  </div>
                )}

                {/* Step 3: Guardian */}
                {currentStep === 3 && (
                  <div className="space-y-4">
                    <div className="bg-ankur-primary/5 rounded-xl p-4 mb-4">
                      <div className="flex items-center gap-3">
                        <Users className="h-6 w-6 text-ankur-primary" />
                        <div>
                          <p className="font-medium text-ankur-secondary">Parent/Guardian Information</p>
                          <p className="text-sm text-gray-600">We&apos;ll keep them informed about your progress</p>
                        </div>
                      </div>
                    </div>
                    <div>
                      <Label htmlFor="guardianName" className="text-gray-700">Guardian&apos;s Full Name</Label>
                      <Input id="guardianName" {...register('guardianName')} placeholder="Enter guardian's full name" className={`mt-1.5 h-11 ${errors.guardianName ? 'border-red-500' : 'border-gray-200'}`} />
                      {errors.guardianName && <p className="text-sm text-red-500 mt-1">{errors.guardianName.message}</p>}
                    </div>
                    <div>
                      <Label htmlFor="guardianContact" className="text-gray-700">Guardian&apos;s Contact Number</Label>
                      <Input id="guardianContact" {...register('guardianContact')} placeholder="Enter mobile number" className={`mt-1.5 h-11 ${errors.guardianContact ? 'border-red-500' : 'border-gray-200'}`} />
                      {errors.guardianContact && <p className="text-sm text-red-500 mt-1">{errors.guardianContact.message}</p>}
                    </div>
                  </div>
                )}

                {/* Step 4: Goals */}
                {currentStep === 4 && (
                  <div className="space-y-4">
                    <div>
                      <Label className="text-gray-700 mb-3 block">What are your learning goals?</Label>
                      <div className="grid grid-cols-2 gap-3">
                        {learningGoals.map((goal) => {
                          const isChecked = watchedGoals.includes(goal);
                          return (
                            <label 
                              key={goal} 
                              htmlFor={`goal-${goal}`}
                              className={`flex items-center gap-3 p-3 rounded-xl border-2 cursor-pointer transition-all ${isChecked ? 'border-ankur-primary bg-ankur-primary/5' : 'border-gray-100 hover:border-gray-200'}`}
                            >
                              <input
                                type="checkbox"
                                id={`goal-${goal}`}
                                checked={isChecked}
                                onChange={(e) => handleGoalChange(goal, e.target.checked)}
                                className="h-4 w-4 rounded border-gray-300 text-ankur-primary focus:ring-ankur-primary"
                              />
                              <span className="text-sm text-gray-700">{goal}</span>
                            </label>
                          );
                        })}
                      </div>
                      {errors.goals && <p className="text-sm text-red-500 mt-2">{errors.goals.message}</p>}
                    </div>
                    <label 
                      htmlFor="acceptTerms"
                      className={`flex items-start gap-3 p-4 rounded-xl border-2 cursor-pointer ${watchedAcceptTerms ? 'border-ankur-primary bg-ankur-primary/5' : 'border-gray-100'}`}
                    >
                      <input
                        type="checkbox"
                        id="acceptTerms"
                        checked={watchedAcceptTerms}
                        onChange={(e) => setValue('acceptTerms', e.target.checked)}
                        className="mt-0.5 h-4 w-4 rounded border-gray-300 text-ankur-primary focus:ring-ankur-primary"
                      />
                      <span className="text-sm text-gray-600">
                        I agree to the <a href="#" className="text-ankur-primary hover:underline font-medium">Terms and Conditions</a> and <a href="#" className="text-ankur-primary hover:underline font-medium">Privacy Policy</a>
                      </span>
                    </label>
                    {errors.acceptTerms && <p className="text-sm text-red-500">{errors.acceptTerms.message}</p>}
                  </div>
                )}

                {/* Navigation */}
                <div className="flex justify-between pt-4">
                  <Button type="button" variant="outline" onClick={prevStep} disabled={currentStep === 1} className="border-gray-200 text-gray-600 hover:bg-gray-50">
                    <ArrowLeft className="h-4 w-4 mr-2" />Back
                  </Button>
                  {currentStep < steps.length ? (
                    <Button type="button" onClick={nextStep} className="btn-primary">Continue<ArrowRight className="h-4 w-4 ml-2" /></Button>
                  ) : (
                    <Button type="submit" disabled={isLoading} className="btn-primary">
                      {isLoading ? <><Loader2 className="h-4 w-4 mr-2 animate-spin" />Creating...</> : 'Create Account'}
                    </Button>
                  )}
                </div>
              </form>

              <div className="mt-8 pt-6 border-t border-gray-100 text-center">
                <p className="text-gray-600">Already have an account? <Link href="/login" className="text-ankur-primary font-semibold hover:underline">Sign in</Link></p>
              </div>
            </CardContent>
          </Card>

          <p className="text-center text-gray-500 text-sm mt-6">Looking to teach? <Link href="/register-teacher" className="text-ankur-primary hover:underline">Register as a teacher</Link></p>
        </div>
      </div>
    </div>
  );
}
