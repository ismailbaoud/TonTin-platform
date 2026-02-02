import logo from '../assets/logo.png'

export default function Navbar() {
  return (
    <nav className="bg-white border-b border-gray-200" >
      <div className="px-6 py-4 flex items-center ">
        <img
          src={logo}
          alt="TONTIN Logo"
          className="h-14 w-auto"
        />
        <p className="text-xl font-semibold tracking-wide">
          Tontin
        </p>
      </div>

    </nav>
  )
}