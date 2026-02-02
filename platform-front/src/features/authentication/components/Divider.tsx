function Divider() {
    return (
        <div className="relative my-8">
            <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-100 dark:border-[#2a4030]"></div>
            </div>
            <div className="relative flex justify-center text-xs uppercase">
                <span className="bg-white dark:bg-[#1a2c20] px-2 text-gray-400 dark:text-gray-500">Or</span>
            </div>
        </div>
    );

}

export default Divider;