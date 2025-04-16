import SearchForm from "@/components/search-form"

export default function Home() {
  return (
    <div className="min-h-screen flex flex-col relative overflow-hidden">
      <video
        autoPlay
        loop
        muted
        playsInline
        preload="auto"
        disablePictureInPicture
        className="absolute inset-0 w-full h-full object-cover z-[-1]"
      >
        <source
          src="/videos/background-mobile.mp4"
          type="video/mp4"
          media="(max-width: 640px)"
        />
        <source
          src="/videos/background.mp4"
          type="video/mp4"
        />
        Your browser does not support the video tag.
      </video>
      <div className="fixed top-0 left-0 w-full h-full bg-black/50 z-[-1]" />

      <header className="w-full max-w-3xl mx-auto p-4 text-center z-10">
        <h1 className="text-4xl font-bold text-white/80">middiff.gg</h1>
      </header>

      <main className="flex-grow flex items-center justify-center z-10">
        <div className="w-full max-w-2xl px-4 py-6 bg-transparent shadow rounded">
          <h2 className="text-2xl text-white font-semibold text-center mb-4">Player Search</h2>
          <p className="text-center text-white font-bold mb-6">
            Enter a Riot ID and tagline to find a player
          </p>
          <SearchForm />
        </div>
      </main>

      <footer className="p-4 text-sm text-center text-gray-300 z-10 relative">
        <p>© {new Date().getFullYear()} middiff.gg</p>
        <p>
          middiff.gg is not endorsed by Riot Games and does not reflect the views or opinions of Riot Games or anyone officially involved in producing or managing Riot Games properties. Riot Games and all associated properties are trademarks or registered trademarks of Riot Games, Inc.
        </p>
      </footer>
    </div>
  );
}

