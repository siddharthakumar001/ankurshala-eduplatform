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
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { useAuthStore } from '@/store/auth';
import {
  Loader2,
  ArrowLeft,
  ArrowRight,
  Check,
  Users,
  BookOpen,
  Clock,
  DollarSign,
  Star,
  Shield,
  Plus,
  X,
  Briefcase,
  Calendar,
} from 'lucide-react';

const languages = ['English', 'Hindi', 'Bengali', 'Tamil', 'Telugu', 'Marathi', 'Gujarati', 'Kannada', 'Malayalam'];
const categories = ['School Tutoring', 'Competitive Exams', 'Languages', 'Arts & Music', 'Technology', 'Other'];
const boards = ['CBSE', 'ICSE', 'IB', 'Cambridge', 'State Board', 'NIOS'];
const grades = ['Class 1-5', 'Class 6-8', 'Class 9-10', 'Class 11-12'];
const subjects = [
  { id: 1, name: 'Mathematics' },
  { id: 2, name: 'Physics' },
  { id: 3, name: 'Chemistry' },
  { id: 4, name: 'Biology' },
  { id: 5, name: 'English' },
  { id: 6, name: 'Hindi' },
  { id: 7, name: 'Computer Science' },
  { id: 8, name: 'Economics' },
  { id: 9, name: 'Accountancy' },
  { id: 10, name: 'Social Science' },
];

const weekdays = [
  { value: 1, label: 'Monday' },
  { value: 2, label: 'Tuesday' },
  { value: 3, label: 'Wednesday' },
  { value: 4, label: 'Thursday' },
  { value: 5, label: 'Friday' },
  { value: 6, label: 'Saturday' },
  { value: 7, label: 'Sunday' },
];

const steps = [
  { id: 1, title: 'Profile', description: 'Basic info' },
  { id: 2, title: 'Subjects', description: 'Expertise' },
  { id: 3, title: 'Availability', description: 'Schedule' },
  { id: 4, title: 'Review', description: 'Confirm' },
];

const teacherSignupSchema = z.object({
  name: z.string().min(2, 'Name must be at least 2 characters'),
  email: z.string().email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  confirmPassword: z.string().min(6, 'Password must be at least 6 characters'),
  bio: z.string().min(50, 'Bio must be at least 50 characters'),
  yearsExperience: z.number().min(0, 'Years of experience must be a positive number'),
  languages: z.array(z.string()).min(1, 'Please select at least one language'),
  categories: z.array(z.string()).min(1, 'Please select at least one category'),
  hourlyRate: z.number().min(100, 'Hourly rate must be at least INR 100'),
  subjectExpertise: z.array(z.object({
    board: z.string(),
    grade: z.string(),
    subjectId: z.number(),
    language: z.string(),
  })).min(1, 'Please add at least one subject expertise'),
  availability: z.array(z.object({
    weekday: z.number(),
    startTime: z.string(),
    endTime: z.string(),
    timezone: z.string(),
  })).min(1, 'Please add at least one availability slot'),
  acceptTerms: z.boolean().refine(val => val === true, { message: 'You must accept the terms' }),
});

type TeacherSignupForm = z.infer<typeof teacherSignupSchema>;

export default function RegisterTeacherPage() {
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
  } = useForm<TeacherSignupForm>({
    resolver: zodResolver(teacherSignupSchema),
    mode: 'onChange',
    defaultValues: {
      subjectExpertise: [{ board: '', grade: '', subjectId: 0, language: '' }],
      availability: [{ weekday: 1, startTime: '', endTime: '', timezone: 'Asia/Kolkata' }],
    },
  });

  const watchedLanguages = watch('languages') || [];
  const watchedCategories = watch('categories') || [];
  const watchedSubjectExpertise = watch('subjectExpertise') || [];
  const watchedAvailability = watch('availability') || [];
  const watchedAcceptTerms = watch('acceptTerms');

  const handleLanguageChange = (language: string, checked: boolean) => {
    const currentLanguages = watchedLanguages;
    if (checked) {
      setValue('languages', [...currentLanguages, language]);
    } else {
      setValue('languages', currentLanguages.filter(l => l !== language));
    }
  };

  const handleCategoryChange = (category: string, checked: boolean) => {
    const currentCategories = watchedCategories;
    if (checked) {
      setValue('categories', [...currentCategories, category]);
    } else {
      setValue('categories', currentCategories.filter(c => c !== category));
    }
  };

  const addSubjectExpertise = () => {
    setValue('subjectExpertise', [
      ...watchedSubjectExpertise,
      { board: '', grade: '', subjectId: 0, language: '' }
    ]);
  };

  const removeSubjectExpertise = (index: number) => {
    const updated = watchedSubjectExpertise.filter((_, i) => i !== index);
    setValue('subjectExpertise', updated);
  };

  const updateSubjectExpertise = (index: number, field: string, value: any) => {
    const updated = watchedSubjectExpertise.map((item, i) => 
      i === index ? { ...item, [field]: value } : item
    );
    setValue('subjectExpertise', updated);
  };

  const addAvailability = () => {
    setValue('availability', [
      ...watchedAvailability,
      { weekday: 1, startTime: '', endTime: '', timezone: 'Asia/Kolkata' }
    ]);
  };

  const removeAvailability = (index: number) => {
    const updated = watchedAvailability.filter((_, i) => i !== index);
    setValue('availability', updated);
  };

  const updateAvailability = (index: number, field: string, value: any) => {
    const updated = watchedAvailability.map((item, i) => 
      i === index ? { ...item, [field]: value } : item
    );
    setValue('availability', updated);
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

  const getFieldsForStep = (step: number): (keyof TeacherSignupForm)[] => {
    switch (step) {
      case 1:
        return ['name', 'email', 'password', 'confirmPassword', 'bio', 'yearsExperience', 'languages', 'categories', 'hourlyRate'];
      case 2:
        return ['subjectExpertise'];
      case 3:
        return ['availability'];
      case 4:
        return ['acceptTerms'];
      default:
        return [];
    }
  };

  const onSubmit = async (data: TeacherSignupForm) => {
    if (data.password !== data.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      await signup('teacher', {
        name: data.name,
        email: data.email,
        password: data.password,
        bio: data.bio,
        yearsExperience: data.yearsExperience,
        languages: data.languages,
        categories: data.categories,
        hourlyRate: data.hourlyRate,
        subjectExpertise: data.subjectExpertise,
        availability: data.availability,
      });

      router.push('/teacher/dashboard');
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Signup failed. Please try again.';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const features = [
    { icon: Users, text: 'Connect with 5,000+ students' },
    { icon: DollarSign, text: 'Set your own hourly rates' },
    { icon: Clock, text: 'Flexible scheduling' },
    { icon: Star, text: 'Build your reputation' },
  ];

  return (
    <div className="min-h-screen flex bg-transparent text-foreground">
      {/* Left Brand Section */}
      <div className="hidden lg:flex lg:w-5/12 brand-gradient p-12 flex-col justify-between relative overflow-hidden">
        {/* Decorative elements */}
        <div className="absolute top-0 right-0 w-96 h-96 bg-ankur-primary/20 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2" />
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-ankur-accent/20 rounded-full blur-3xl translate-y-1/2 -translate-x-1/2" />
        
        <div className="relative z-10">
          <Link href="/" className="flex items-center gap-3 mb-12">
            <Image src="/ankurshala-logo-small.png" width={56} height={56} alt="Ankurshala" className="rounded-lg" />
            <span className="text-2xl font-display font-semibold text-white">Ankurshala</span>
          </Link>
          
          <h1 className="text-4xl font-bold text-white mb-4">
            Share Your Knowledge
          </h1>
          <p className="text-white/80 text-lg mb-12">
            Join our community of expert educators and help students achieve their academic goals while earning on your own terms.
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
            <p className="text-white font-semibold">Trusted Platform</p>
            <p className="text-white/70 text-sm">Join 500+ verified teachers already on Ankurshala</p>
          </div>
        </div>
      </div>

      {/* Right Form Section */}
      <div className="flex-1 flex items-center justify-center p-6 lg:p-12 bg-transparent overflow-y-auto">
        <div className="w-full max-w-2xl">
          {/* Mobile header */}
          <div className="lg:hidden mb-8 text-center">
            <Link href="/" className="inline-flex items-center gap-2 mb-4">
              <Image src="/ankurshala-logo-small.png" width={40} height={40} alt="Ankurshala" className="rounded-lg" />
              <span className="text-xl font-display font-semibold text-ankur-secondary dark:text-white">Ankurshala</span>
            </Link>
          </div>

          <Card className="border-0 shadow-xl glass-panel">
            <CardContent className="p-8">
              <div className="text-center mb-8">
                <div className="inline-flex items-center justify-center w-14 h-14 rounded-xl bg-ankur-secondary/10 mb-4">
                  <Briefcase className="h-7 w-7 text-ankur-secondary" />
                </div>
                <h2 className="text-2xl font-display font-semibold text-ankur-secondary dark:text-white">Teacher Registration</h2>
                <p className="text-gray-600 dark:text-gray-300 mt-1 dark:text-gray-300">Create your teaching profile in 4 steps</p>
              </div>

              {/* Progress Steps */}
              <div className="mb-8">
                <div className="flex items-center justify-between">
                  {steps.map((step, index) => (
                    <div key={step.id} className="flex items-center flex-1">
                      <div className="flex flex-col items-center flex-1">
                        <div className={`flex items-center justify-center w-10 h-10 rounded-full transition-all ${
                          currentStep >= step.id 
                            ? 'bg-ankur-secondary text-white' 
                            : 'bg-gray-100 text-gray-400'
                        }`}>
                          {currentStep > step.id ? (
                            <Check className="h-5 w-5" />
                          ) : (
                            <span className="text-sm font-semibold">{step.id}</span>
                          )}
                        </div>
                        <div className="mt-2 text-center hidden sm:block">
                          <p className={`text-xs font-medium ${
                            currentStep >= step.id ? 'text-ankur-secondary' : 'text-gray-400'
                          }`}>
                            {step.title}
                          </p>
                        </div>
                      </div>
                      {index < steps.length - 1 && (
                        <div className={`h-1 flex-1 mx-2 rounded ${
                          currentStep > step.id ? 'bg-ankur-secondary' : 'bg-gray-100'
                        }`} />
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
                {/* Step 1: Profile */}
                {currentStep === 1 && (
                  <div className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label htmlFor="name" className="text-gray-700 dark:text-gray-200">Full Name</Label>
                        <Input
                          id="name"
                          {...register('name')}
                          placeholder="Enter your full name"
                          className={`mt-1.5 h-11 ${errors.name ? 'border-red-500' : 'border-gray-200'}`}
                        />
                        {errors.name && (
                          <p className="text-sm text-red-500 mt-1">{errors.name.message}</p>
                        )}
                      </div>

                      <div>
                        <Label htmlFor="email" className="text-gray-700 dark:text-gray-200">Email Address</Label>
                        <Input
                          id="email"
                          type="email"
                          {...register('email')}
                          placeholder="Enter your email"
                          className={`mt-1.5 h-11 ${errors.email ? 'border-red-500' : 'border-gray-200'}`}
                        />
                        {errors.email && (
                          <p className="text-sm text-red-500 mt-1">{errors.email.message}</p>
                        )}
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label htmlFor="password" className="text-gray-700 dark:text-gray-200">Password</Label>
                        <Input
                          id="password"
                          type="password"
                          {...register('password')}
                          placeholder="Create password"
                          className={`mt-1.5 h-11 ${errors.password ? 'border-red-500' : 'border-gray-200'}`}
                        />
                        {errors.password && (
                          <p className="text-sm text-red-500 mt-1">{errors.password.message}</p>
                        )}
                      </div>

                      <div>
                        <Label htmlFor="confirmPassword" className="text-gray-700 dark:text-gray-200">Confirm Password</Label>
                        <Input
                          id="confirmPassword"
                          type="password"
                          {...register('confirmPassword')}
                          placeholder="Confirm password"
                          className={`mt-1.5 h-11 ${errors.confirmPassword ? 'border-red-500' : 'border-gray-200'}`}
                        />
                        {errors.confirmPassword && (
                          <p className="text-sm text-red-500 mt-1">{errors.confirmPassword.message}</p>
                        )}
                      </div>
                    </div>

                    <div>
                      <Label htmlFor="bio" className="text-gray-700 dark:text-gray-200">Bio / About You</Label>
                      <Textarea
                        id="bio"
                        {...register('bio')}
                        placeholder="Tell us about your teaching experience, qualifications, and teaching philosophy (minimum 50 characters)"
                        className={`mt-1.5 min-h-[100px] ${errors.bio ? 'border-red-500' : 'border-gray-200'}`}
                      />
                      {errors.bio && (
                        <p className="text-sm text-red-500 mt-1">{errors.bio.message}</p>
                      )}
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label htmlFor="yearsExperience" className="text-gray-700 dark:text-gray-200">Years of Experience</Label>
                        <Input
                          id="yearsExperience"
                          type="number"
                          {...register('yearsExperience', { valueAsNumber: true })}
                          placeholder="e.g., 5"
                          className={`mt-1.5 h-11 ${errors.yearsExperience ? 'border-red-500' : 'border-gray-200'}`}
                        />
                        {errors.yearsExperience && (
                          <p className="text-sm text-red-500 mt-1">{errors.yearsExperience.message}</p>
                        )}
                      </div>

                      <div>
                        <Label htmlFor="hourlyRate" className="text-gray-700 dark:text-gray-200">Hourly Rate (INR)</Label>
                        <Input
                          id="hourlyRate"
                          type="number"
                          {...register('hourlyRate', { valueAsNumber: true })}
                          placeholder="e.g., 500"
                          className={`mt-1.5 h-11 ${errors.hourlyRate ? 'border-red-500' : 'border-gray-200'}`}
                        />
                        {errors.hourlyRate && (
                          <p className="text-sm text-red-500 mt-1">{errors.hourlyRate.message}</p>
                        )}
                      </div>
                    </div>

                    <div>
                      <Label className="text-gray-700 dark:text-gray-200 mb-2 block">Languages You Can Teach In</Label>
                      <div className="flex flex-wrap gap-2">
                        {languages.map((language) => (
                          <div 
                            key={language}
                            onClick={() => handleLanguageChange(language, !watchedLanguages.includes(language))}
                            className={`px-4 py-2 rounded-full text-sm cursor-pointer transition-all border ${
                              watchedLanguages.includes(language)
                                ? 'bg-ankur-secondary text-white border-ankur-secondary'
                                : 'bg-gray-50 text-gray-600 dark:text-gray-300 border-gray-200 hover:border-ankur-secondary'
                            }`}
                          >
                            {language}
                          </div>
                        ))}
                      </div>
                      {errors.languages && (
                        <p className="text-sm text-red-500 mt-1">{errors.languages.message}</p>
                      )}
                    </div>

                    <div>
                      <Label className="text-gray-700 dark:text-gray-200 mb-2 block">Teaching Categories</Label>
                      <div className="flex flex-wrap gap-2">
                        {categories.map((category) => (
                          <div 
                            key={category}
                            onClick={() => handleCategoryChange(category, !watchedCategories.includes(category))}
                            className={`px-4 py-2 rounded-full text-sm cursor-pointer transition-all border ${
                              watchedCategories.includes(category)
                                ? 'bg-ankur-primary text-white border-ankur-primary'
                                : 'bg-gray-50 text-gray-600 dark:text-gray-300 border-gray-200 hover:border-ankur-primary'
                            }`}
                          >
                            {category}
                          </div>
                        ))}
                      </div>
                      {errors.categories && (
                        <p className="text-sm text-red-500 mt-1">{errors.categories.message}</p>
                      )}
                    </div>
                  </div>
                )}

                {/* Step 2: Subjects */}
                {currentStep === 2 && (
                  <div className="space-y-4">
                    <div className="flex items-center justify-between mb-4">
                      <div className="flex items-center gap-3">
                        <BookOpen className="h-5 w-5 text-ankur-secondary" />
                        <span className="font-medium text-ankur-secondary">Subject Expertise</span>
                      </div>
                      <Button type="button" variant="outline" size="sm" onClick={addSubjectExpertise} className="border-ankur-secondary text-ankur-secondary hover:bg-ankur-secondary/5">
                        <Plus className="h-4 w-4 mr-1" />
                        Add Subject
                      </Button>
                    </div>

                    {watchedSubjectExpertise.map((expertise, index) => (
                      <div key={index} className="bg-gray-50 rounded-xl p-4 border border-gray-100">
                        <div className="flex items-center justify-between mb-4">
                          <span className="font-medium text-gray-700 dark:text-gray-200">Subject {index + 1}</span>
                          {watchedSubjectExpertise.length > 1 && (
                            <Button
                              type="button"
                              variant="ghost"
                              size="sm"
                              onClick={() => removeSubjectExpertise(index)}
                              className="text-gray-400 hover:text-red-500"
                            >
                              <X className="h-4 w-4" />
                            </Button>
                          )}
                        </div>

                        <div className="grid grid-cols-2 gap-3">
                          <div>
                            <Label className="text-gray-600 dark:text-gray-300 text-sm">Board</Label>
                            <Select onValueChange={(value) => updateSubjectExpertise(index, 'board', value)}>
                              <SelectTrigger className="mt-1 h-10 border-gray-200">
                                <SelectValue placeholder="Select board" />
                              </SelectTrigger>
                              <SelectContent>
                                {boards.map((board) => (
                                  <SelectItem key={board} value={board}>{board}</SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                          </div>

                          <div>
                            <Label className="text-gray-600 dark:text-gray-300 text-sm">Grade</Label>
                            <Select onValueChange={(value) => updateSubjectExpertise(index, 'grade', value)}>
                              <SelectTrigger className="mt-1 h-10 border-gray-200">
                                <SelectValue placeholder="Select grade" />
                              </SelectTrigger>
                              <SelectContent>
                                {grades.map((grade) => (
                                  <SelectItem key={grade} value={grade}>{grade}</SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                          </div>

                          <div>
                            <Label className="text-gray-600 dark:text-gray-300 text-sm">Subject</Label>
                            <Select onValueChange={(value) => updateSubjectExpertise(index, 'subjectId', parseInt(value))}>
                              <SelectTrigger className="mt-1 h-10 border-gray-200">
                                <SelectValue placeholder="Select subject" />
                              </SelectTrigger>
                              <SelectContent>
                                {subjects.map((subject) => (
                                  <SelectItem key={subject.id} value={subject.id.toString()}>{subject.name}</SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                          </div>

                          <div>
                            <Label className="text-gray-600 dark:text-gray-300 text-sm">Language</Label>
                            <Select onValueChange={(value) => updateSubjectExpertise(index, 'language', value)}>
                              <SelectTrigger className="mt-1 h-10 border-gray-200">
                                <SelectValue placeholder="Select language" />
                              </SelectTrigger>
                              <SelectContent>
                                {languages.map((language) => (
                                  <SelectItem key={language} value={language}>{language}</SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                          </div>
                        </div>
                      </div>
                    ))}

                    {errors.subjectExpertise && (
                      <p className="text-sm text-red-500">{errors.subjectExpertise.message}</p>
                    )}
                  </div>
                )}

                {/* Step 3: Availability */}
                {currentStep === 3 && (
                  <div className="space-y-4">
                    <div className="flex items-center justify-between mb-4">
                      <div className="flex items-center gap-3">
                        <Calendar className="h-5 w-5 text-ankur-secondary" />
                        <span className="font-medium text-ankur-secondary">Your Availability</span>
                      </div>
                      <Button type="button" variant="outline" size="sm" onClick={addAvailability} className="border-ankur-secondary text-ankur-secondary hover:bg-ankur-secondary/5">
                        <Plus className="h-4 w-4 mr-1" />
                        Add Slot
                      </Button>
                    </div>

                    {watchedAvailability.map((slot, index) => (
                      <div key={index} className="bg-gray-50 rounded-xl p-4 border border-gray-100">
                        <div className="flex items-center justify-between mb-4">
                          <span className="font-medium text-gray-700 dark:text-gray-200">Time Slot {index + 1}</span>
                          {watchedAvailability.length > 1 && (
                            <Button
                              type="button"
                              variant="ghost"
                              size="sm"
                              onClick={() => removeAvailability(index)}
                              className="text-gray-400 hover:text-red-500"
                            >
                              <X className="h-4 w-4" />
                            </Button>
                          )}
                        </div>

                        <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                          <div>
                            <Label className="text-gray-600 dark:text-gray-300 text-sm">Day</Label>
                            <Select onValueChange={(value) => updateAvailability(index, 'weekday', parseInt(value))}>
                              <SelectTrigger className="mt-1 h-10 border-gray-200">
                                <SelectValue placeholder="Select day" />
                              </SelectTrigger>
                              <SelectContent>
                                {weekdays.map((day) => (
                                  <SelectItem key={day.value} value={day.value.toString()}>{day.label}</SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                          </div>

                          <div>
                            <Label className="text-gray-600 dark:text-gray-300 text-sm">Start Time</Label>
                            <Input
                              type="time"
                              value={slot.startTime}
                              onChange={(e) => updateAvailability(index, 'startTime', e.target.value)}
                              className="mt-1 h-10 border-gray-200"
                            />
                          </div>

                          <div>
                            <Label className="text-gray-600 dark:text-gray-300 text-sm">End Time</Label>
                            <Input
                              type="time"
                              value={slot.endTime}
                              onChange={(e) => updateAvailability(index, 'endTime', e.target.value)}
                              className="mt-1 h-10 border-gray-200"
                            />
                          </div>

                          <div>
                            <Label className="text-gray-600 dark:text-gray-300 text-sm">Timezone</Label>
                            <Select onValueChange={(value) => updateAvailability(index, 'timezone', value)}>
                              <SelectTrigger className="mt-1 h-10 border-gray-200">
                                <SelectValue placeholder="Select timezone" />
                              </SelectTrigger>
                              <SelectContent>
                                <SelectItem value="Asia/Kolkata">Asia/Kolkata</SelectItem>
                                <SelectItem value="Asia/Dubai">Asia/Dubai</SelectItem>
                                <SelectItem value="America/New_York">America/New_York</SelectItem>
                                <SelectItem value="Europe/London">Europe/London</SelectItem>
                              </SelectContent>
                            </Select>
                          </div>
                        </div>
                      </div>
                    ))}

                    {errors.availability && (
                      <p className="text-sm text-red-500">{errors.availability.message}</p>
                    )}
                  </div>
                )}

                {/* Step 4: Review */}
                {currentStep === 4 && (
                  <div className="space-y-4">
                    <div className="bg-ankur-secondary/5 rounded-xl p-6 space-y-4">
                      <h4 className="font-semibold text-ankur-secondary text-lg">Profile Summary</h4>
                      <div className="grid grid-cols-2 gap-4 text-sm">
                        <div>
                          <span className="text-gray-500 dark:text-gray-400">Name:</span>
                          <p className="font-medium text-gray-700 dark:text-gray-200">{watch('name')}</p>
                        </div>
                        <div>
                          <span className="text-gray-500 dark:text-gray-400">Email:</span>
                          <p className="font-medium text-gray-700 dark:text-gray-200">{watch('email')}</p>
                        </div>
                        <div>
                          <span className="text-gray-500 dark:text-gray-400">Experience:</span>
                          <p className="font-medium text-gray-700 dark:text-gray-200">{watch('yearsExperience')} years</p>
                        </div>
                        <div>
                          <span className="text-gray-500 dark:text-gray-400">Hourly Rate:</span>
                          <p className="font-medium text-gray-700 dark:text-gray-200">INR {watch('hourlyRate')}</p>
                        </div>
                        <div className="col-span-2">
                          <span className="text-gray-500 dark:text-gray-400">Languages:</span>
                          <p className="font-medium text-gray-700 dark:text-gray-200">{watchedLanguages.join(', ') || 'None selected'}</p>
                        </div>
                        <div className="col-span-2">
                          <span className="text-gray-500 dark:text-gray-400">Categories:</span>
                          <p className="font-medium text-gray-700 dark:text-gray-200">{watchedCategories.join(', ') || 'None selected'}</p>
                        </div>
                        <div>
                          <span className="text-gray-500 dark:text-gray-400">Subjects:</span>
                          <p className="font-medium text-gray-700 dark:text-gray-200">{watchedSubjectExpertise.length} subject(s)</p>
                        </div>
                        <div>
                          <span className="text-gray-500 dark:text-gray-400">Availability:</span>
                          <p className="font-medium text-gray-700 dark:text-gray-200">{watchedAvailability.length} time slot(s)</p>
                        </div>
                      </div>
                    </div>

                    <div className={`flex items-start gap-3 p-4 rounded-xl border-2 ${
                      watchedAcceptTerms ? 'border-ankur-secondary bg-ankur-secondary/5' : 'border-gray-100'
                    }`}>
                      <Checkbox
                        id="acceptTerms"
                        checked={Boolean(watchedAcceptTerms)}
                        onCheckedChange={(checked) => setValue('acceptTerms', checked as boolean)}
                        className="mt-0.5"
                      />
                      <Label htmlFor="acceptTerms" className="text-sm text-gray-600 dark:text-gray-300 cursor-pointer">
                        I agree to the{' '}
                        <a href="#" className="text-ankur-secondary hover:underline font-medium">Terms and Conditions</a>
                        {' '}and{' '}
                        <a href="#" className="text-ankur-secondary hover:underline font-medium">Privacy Policy</a>
                      </Label>
                    </div>
                    {errors.acceptTerms && (
                      <p className="text-sm text-red-500">{errors.acceptTerms.message}</p>
                    )}
                  </div>
                )}

                {/* Navigation */}
                <div className="flex justify-between pt-4">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={prevStep}
                    disabled={currentStep === 1}
                    className="border-gray-200 text-gray-600 dark:text-gray-300 hover:bg-gray-50"
                  >
                    <ArrowLeft className="h-4 w-4 mr-2" />
                    Back
                  </Button>

                  {currentStep < steps.length ? (
                    <Button 
                      type="button" 
                      onClick={nextStep}
                      className="bg-ankur-secondary hover:bg-ankur-secondary/90 text-white"
                    >
                      Continue
                      <ArrowRight className="h-4 w-4 ml-2" />
                    </Button>
                  ) : (
                    <Button 
                      type="submit" 
                      disabled={isLoading}
                      className="bg-ankur-secondary hover:bg-ankur-secondary/90 text-white"
                    >
                      {isLoading ? (
                        <>
                          <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                          Creating...
                        </>
                      ) : (
                        'Create Account'
                      )}
                    </Button>
                  )}
                </div>
              </form>

              <div className="mt-8 pt-6 border-t border-gray-100 text-center">
                <p className="text-gray-600 dark:text-gray-300">
                  Already have an account? {' '}
                  <Link href="/login" className="text-ankur-secondary font-semibold hover:underline">
                    Sign in
                  </Link>
                </p>
              </div>
            </CardContent>
          </Card>

          <p className="text-center text-gray-500 dark:text-gray-400 text-sm mt-6">
            Looking to learn? {' '}
            <Link href="/register-student" className="text-ankur-primary hover:underline">
              Register as a student
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
