import SearchForm from "@/components/search-form"

export default function Home() {
  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <header className="w-full max-w-3xl mx-auto p-4 text-center">
        <h1 className="text-4xl font-bold text-gray-800">middiff.gg</h1>
      </header>

      <main className="flex-grow flex items-center justify-center">
        <div className="w-full max-w-2xl px-4 py-6 bg-white shadow rounded">
          <h2 className="text-2xl text-black font-semibold text-center mb-4">Player Search</h2>
          <p className="text-center text-gray-600 mb-6">
            Enter a Riot ID and tagline to find a player
          </p>
          <SearchForm />
        </div>
      </main>

      <footer className="p-4 text-sm text-center text-gray-500">
        <p>© {new Date().getFullYear()} middiff.gg</p>
        <p>
          middiff.gg is not endorsed by Riot Games and does not reflect the views or opinions of Riot Games or anyone officially involved in producing or managing Riot Games properties. Riot Games and all associated properties are trademarks or registered trademarks of Riot Games, Inc.
        </p>
      </footer>
    </div>
  );
}
