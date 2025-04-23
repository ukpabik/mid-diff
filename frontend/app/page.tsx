"use client";

import SearchForm from "@/components/search-form";
import BackgroundToggle from "@/components/background";
import NavTabs from "@/components/nav-tab";

export default function Home() {

  return (
    <div className="min-h-screen flex flex-col relative overflow-hidden">
      <BackgroundToggle opacity={30} />

      <header className="w-full max-w-3xl mx-auto p-4 text-center z-10">
        <h1 className="text-4xl font-bold text-white/80">middiff.gg</h1>
      </header>
      <NavTabs />

      <main className="flex-grow flex items-center justify-center z-10">
        <div className="w-full max-w-2xl px-4 py-6 bg-transparent shadow rounded">
          <h2 className="text-2xl text-white font-semibold text-center mb-4">
            Player Search
          </h2>
          <p className="text-center text-white font-bold mb-6">
            Enter a Riot ID and tagline to find a player
          </p>
          <SearchForm />
        </div>
      </main>

      <footer className="p-4 text-sm text-center text-gray-300 z-10 relative">
        <p>© {new Date().getFullYear()} middiff.gg</p>
        <p>
          middiff.gg is not endorsed by Riot Games and does not reflect the
          views or opinions of Riot Games or anyone officially involved in
          producing or managing Riot Games properties. Riot Games and all
          associated properties are trademarks or registered trademarks of Riot
          Games, Inc.
        </p>
      </footer>
    </div>
  );
}
