"use client";

import { useState } from "react";
import { Cog } from "lucide-react";
import SearchForm from "@/components/search-form";

import {
  Popover,
  PopoverTrigger,
  PopoverContent,
} from "@/components/ui/popover";
import { Switch } from "@/components/ui/switch";

export default function Home() {
  const [showVideo, setShowVideo] = useState(true);

  return (
    <div className="min-h-screen flex flex-col relative overflow-hidden">
      <div
        className="absolute inset-0 bg-cover bg-center sm:hidden z-[-1]"
        style={{ backgroundImage: "url('/videos/bg-still.jpg')" }}
      />
      {!showVideo && (
        <div
          className="absolute inset-0 bg-cover bg-center hidden sm:block z-[-1]"
          style={{ backgroundImage: "url('/videos/bg-still.jpg')" }}
        />
      )}
      {showVideo && (
        <video
          autoPlay loop muted playsInline preload="auto"
          disablePictureInPicture
          className="absolute inset-0 w-full h-full object-cover hidden sm:block z-[-1]"
        >
          <source src="/videos/background-1080p.mp4" type="video/mp4" />
        </video>
      )}

      <div className="absolute inset-0 bg-black/30 z-[-1]" />
      <div className="fixed   inset-0 bg-black/30 z-[-1]" />

      <Popover>
        <PopoverTrigger
          className="hidden sm:flex items-center justify-center
                     absolute top-4 right-4 z-20
                     h-9 w-9 rounded-md bg-white/20 hover:bg-white/30
                     backdrop-blur-sm text-white"
        >
          <Cog className="h-5 w-5" />
        </PopoverTrigger>

        <PopoverContent
          side="bottom"
          align="end"
          className="w-52 bg-white/20 backdrop-blur-md border border-white/30"
        >
          <div className="flex items-center justify-between gap-2">
            <span className="text-sm text-white select-none">
              {!showVideo ? "Enable Animation?" : "Disable Animation?"}
            </span>
            <Switch
              checked={showVideo}
              onCheckedChange={setShowVideo}
              id="animated-bg-switch"
            />
          </div>
        </PopoverContent>
      </Popover>

      <header className="w-full max-w-3xl mx-auto p-4 text-center z-10">
        <h1 className="text-4xl font-bold text-white/80">middiff.gg</h1>
      </header>

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
