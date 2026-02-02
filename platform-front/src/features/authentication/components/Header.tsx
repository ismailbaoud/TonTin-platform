function Header() {
    return (
        <div className="px-8 pt-10 pb-6 flex flex-col items-center text-center">
            <div className="flex items-center gap-3 mb-6 justify-center w-full">
            </div>
            <h1 className="text-[#0d1b12] dark:text-gray-100 text-[26px] font-bold leading-tight">
                Welcome back to your savings.
            </h1>
            <p className="text-gray-500 dark:text-gray-400 text-sm mt-2">
                Log in to manage your Dârs and investments
            </p>
        </div>
    );
}

export default Header;
