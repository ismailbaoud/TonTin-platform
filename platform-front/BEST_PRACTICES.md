# React + TypeScript Best Practices

This document outlines the best practices implemented in this project and guidelines for future development.

## ✅ What We Fixed

### 1. **Component Structure & Naming**
- ❌ **Before**: `Password.tsx` exported a component named `Input`
- ✅ **After**: Component name matches file name (`Password`)
- **Why**: Improves code readability and makes imports clearer

### 2. **TypeScript Typing**
- ❌ **Before**: No TypeScript interfaces or types
- ✅ **After**: Proper interfaces for all component props
```typescript
interface EmailInputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, "type"> {
  label?: string;
  error?: string;
}
```
- **Why**: Type safety, better IDE autocomplete, catches bugs at compile time

### 3. **State Management**
- ❌ **Before**: Hardcoded `value=""` in inputs (uncontrolled)
- ✅ **After**: Controlled components with state
```typescript
const [formData, setFormData] = useState<LoginFormData>({
  email: "",
  password: "",
});
```
- **Why**: React best practice, enables validation and dynamic behavior

### 4. **Event Handlers**
- ❌ **Before**: No onChange, onSubmit handlers
- ✅ **After**: Proper event handling
```typescript
const handleEmailChange = (e: React.ChangeEvent<HTMLInputElement>) => {
  setFormData((prev) => ({ ...prev, email: e.target.value }));
};
```
- **Why**: Enables form validation, user feedback, and submission

### 5. **Form Validation**
- ❌ **Before**: No validation
- ✅ **After**: Client-side validation with error messages
```typescript
const validateForm = (): boolean => {
  const newErrors: FormErrors = {};
  if (!formData.email.trim()) {
    newErrors.email = "Email is required";
  }
  return Object.keys(newErrors).length === 0;
};
```
- **Why**: Better UX, prevents invalid submissions

### 6. **Component Reusability**
- ❌ **Before**: Hardcoded text and no props
- ✅ **After**: Flexible components accepting props
```typescript
function Email({ label = "Email or Username", error, ...props }: EmailInputProps) {
  // Component can be reused with different labels and configurations
}
```
- **Why**: DRY principle, easier maintenance

### 7. **Accessibility (a11y)**
- ❌ **Before**: No ARIA attributes
- ✅ **After**: Proper accessibility attributes
```typescript
<input
  aria-label={label}
  aria-invalid={!!error}
  aria-describedby={error ? "email-error" : undefined}
/>
```
- **Why**: Makes the app usable for screen readers and assistive technologies

### 8. **Button Types**
- ❌ **Before**: `<button type="button">` for submit
- ✅ **After**: `<button type="submit">` for form submission
- **Why**: Proper HTML semantics, enables Enter key submission

### 9. **Loading States**
- ❌ **Before**: No loading indicators
- ✅ **After**: Loading state with disabled button
```typescript
const [isLoading, setIsLoading] = useState(false);
<SubmitButton isLoading={isLoading} disabled={isLoading} />
```
- **Why**: Better UX, prevents double submissions

### 10. **Error Handling**
- ❌ **Before**: Hidden error message component
- ✅ **After**: Dynamic error rendering
```typescript
{error && (
  <p role="alert" className="text-red-500 text-xs mt-2">
    {error}
  </p>
)}
```
- **Why**: Users see feedback only when relevant

### 11. **Password Visibility Toggle**
- ❌ **Before**: Static visibility icon
- ✅ **After**: Working toggle with state
```typescript
const [showPassword, setShowPassword] = useState(false);
<input type={showPassword ? "text" : "password"} />
```
- **Why**: Better UX for password entry

## 📋 Best Practices Guidelines

### Component Structure
```typescript
// 1. Imports (type-only imports when possible)
import { useState } from "react";
import type { InputHTMLAttributes } from "react";

// 2. Type/Interface definitions
interface ComponentProps {
  requiredProp: string;
  optionalProp?: number;
}

// 3. Component definition
function Component({ requiredProp, optionalProp = 0 }: ComponentProps) {
  // 4. State declarations
  const [state, setState] = useState<Type>(initialValue);

  // 5. Event handlers
  const handleEvent = () => {
    // logic here
  };

  // 6. JSX return
  return (
    <div>
      {/* Component JSX */}
    </div>
  );
}

// 7. Export
export default Component;
```

### Props Pattern
```typescript
// Extend HTML attributes for native elements
interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  isLoading?: boolean;
  variant?: "primary" | "secondary";
}

// Use destructuring with defaults
function Button({
  isLoading = false,
  variant = "primary",
  children,
  disabled,
  className = "",
  ...props
}: ButtonProps) {
  return (
    <button
      disabled={disabled || isLoading}
      className={`base-styles ${variant} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
```

### Form Handling
```typescript
// 1. Define form data interface
interface FormData {
  email: string;
  password: string;
}

// 2. Define error interface
interface FormErrors {
  email?: string;
  password?: string;
}

// 3. Use controlled components
const [formData, setFormData] = useState<FormData>({
  email: "",
  password: "",
});

const [errors, setErrors] = useState<FormErrors>({});

// 4. Handle form submission
const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
  e.preventDefault();
  
  if (!validateForm()) return;
  
  try {
    await submitForm(formData);
  } catch (error) {
    handleError(error);
  }
};
```

### State Management
```typescript
// ✅ Good: Use functional updates for state derived from previous state
setCount((prev) => prev + 1);

// ❌ Bad: Direct state mutation
count = count + 1;

// ✅ Good: Clear error when user starts typing
const handleChange = (e) => {
  setValue(e.target.value);
  if (errors.field) {
    setErrors((prev) => ({ ...prev, field: undefined }));
  }
};
```

### Validation
```typescript
// Validate early and often
const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

const validateForm = (): boolean => {
  const newErrors: FormErrors = {};
  
  if (!formData.email.trim()) {
    newErrors.email = "Email is required";
  } else if (!validateEmail(formData.email)) {
    newErrors.email = "Invalid email format";
  }
  
  setErrors(newErrors);
  return Object.keys(newErrors).length === 0;
};
```

### Accessibility
```typescript
// Always include:
// 1. Proper labels
<label htmlFor="email">Email</label>
<input id="email" />

// 2. ARIA attributes
<input
  aria-label="Email address"
  aria-invalid={!!error}
  aria-describedby={error ? "email-error" : undefined}
/>

// 3. Error announcements
<p id="email-error" role="alert">
  {error}
</p>

// 4. Button labels
<button aria-label="Show password">
  <span className="material-symbols-outlined">visibility</span>
</button>
```

### TypeScript Tips
```typescript
// 1. Use type-only imports for types (required by some configs)
import type { FC, ReactNode } from "react";

// 2. Omit unnecessary props
interface CustomInputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, "type"> {
  customProp: string;
}

// 3. Use generics for reusable components
interface SelectProps<T> {
  options: T[];
  value: T;
  onChange: (value: T) => void;
}

// 4. Define union types for variants
type ButtonVariant = "primary" | "secondary" | "danger";
```

### Error Handling
```typescript
// Always handle errors gracefully
const handleSubmit = async () => {
  setIsLoading(true);
  setErrors({});

  try {
    const response = await api.login(formData);
    // Handle success
  } catch (error) {
    // Type guard for error handling
    if (error instanceof Error) {
      setErrors({ general: error.message });
    } else {
      setErrors({ general: "An unexpected error occurred" });
    }
  } finally {
    setIsLoading(false);
  }
};
```

### Performance Optimization
```typescript
// 1. Use React.memo for expensive components
export default React.memo(ExpensiveComponent);

// 2. Use useCallback for event handlers passed to children
const handleClick = useCallback(() => {
  // logic
}, [dependencies]);

// 3. Use useMemo for expensive calculations
const sortedList = useMemo(() => {
  return items.sort((a, b) => a.value - b.value);
}, [items]);

// 4. Debounce search inputs
const debouncedSearch = useDebounce(searchTerm, 300);
```

## 🚫 Common Anti-Patterns to Avoid

### 1. Uncontrolled Components
```typescript
// ❌ Bad
<input value="" />

// ✅ Good
<input value={value} onChange={(e) => setValue(e.target.value)} />
```

### 2. Inline Functions in JSX (when passed to child components)
```typescript
// ❌ Bad (causes re-renders)
<Child onClick={() => doSomething()} />

// ✅ Good
const handleClick = useCallback(() => doSomething(), []);
<Child onClick={handleClick} />
```

### 3. Missing Key Props
```typescript
// ❌ Bad
{items.map((item) => <Item item={item} />)}

// ✅ Good
{items.map((item) => <Item key={item.id} item={item} />)}
```

### 4. Direct State Mutation
```typescript
// ❌ Bad
user.name = "John";
setUser(user);

// ✅ Good
setUser({ ...user, name: "John" });
```

### 5. Using Indexes as Keys
```typescript
// ❌ Bad (when order can change)
{items.map((item, index) => <Item key={index} item={item} />)}

// ✅ Good
{items.map((item) => <Item key={item.id} item={item} />)}
```

## 📁 Recommended Folder Structure

```
src/
├── features/              # Feature-based organization
│   ├── authentication/
│   │   ├── components/    # Feature-specific components
│   │   ├── hooks/         # Feature-specific hooks
│   │   ├── services/      # API calls
│   │   ├── context/       # Feature context
│   │   └── index.tsx      # Feature entry point
│   └── dashboard/
├── components/            # Shared/common components
│   ├── Button/
│   │   ├── Button.tsx
│   │   ├── Button.test.tsx
│   │   └── index.ts
│   └── Input/
├── hooks/                 # Shared hooks
│   ├── useAuth.ts
│   └── useDebounce.ts
├── utils/                 # Utility functions
│   ├── validation.ts
│   └── formatting.ts
├── types/                 # Shared TypeScript types
│   └── index.ts
├── services/              # API services
│   └── api.ts
├── layouts/               # Layout components
│   └── MainLayout.tsx
└── pages/                 # Page components
    └── Login.tsx
```

## 🔍 Code Review Checklist

Before submitting code, ensure:
- [ ] All components have TypeScript interfaces
- [ ] Forms are controlled with proper state management
- [ ] Error handling is implemented
- [ ] Loading states are shown during async operations
- [ ] Accessibility attributes are added (aria-*, role)
- [ ] Components accept props for reusability
- [ ] Event handlers are properly typed
- [ ] No console.log statements in production code
- [ ] Components follow single responsibility principle
- [ ] Code is DRY (Don't Repeat Yourself)
- [ ] Tests are written (if applicable)

## 📚 Additional Resources

- [React TypeScript Cheatsheet](https://react-typescript-cheatsheet.netlify.app/)
- [React Best Practices](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)
- [Accessibility Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [TypeScript Handbook](https://www.typescriptlang.org/docs/handbook/intro.html)

## 🎯 Summary

Your components now follow React and TypeScript best practices with:
- ✅ Proper TypeScript typing
- ✅ Controlled components with state management
- ✅ Form validation and error handling
- ✅ Accessibility support
- ✅ Reusable and flexible props
- ✅ Loading states
- ✅ Proper event handling
- ✅ Clean, maintainable code structure

Keep these practices in mind when building new features!