import Register from "../features/authentication";
import Footer from "../features/authentication/components/Footer";
import Navbar from "../layouts/navbar";

function LoginPage() {
  return (
    <>
    <Navbar/>
    <main className="font-display bg-background-light dark:bg-background-dark min-h-screen flex flex-col items-center justify-center p-4">
      <div className="w-full max-w-[480px] bg-white dark:bg-[#1a2c20] rounded-xl shadow-[0_8px_30px_rgb(0,0,0,0.04)] border border-[#e7f3eb] dark:border-[#2a4030] overflow-hidden transition-colors duration-200">
        <Register />
      </div>
      <Footer />
    </main>
    </>
  );
}

export default LoginPage;
