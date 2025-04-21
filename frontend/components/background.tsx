"use client"

import { useState } from "react"
import { Popover, PopoverTrigger, PopoverContent } from "@/components/ui/popover"
import { Switch } from "@/components/ui/switch"
import { Cog } from "lucide-react"

type BackgroundProps = {
  opacity: number
}
export default function BackgroundToggle({ opacity }: BackgroundProps) {
  const [showVideo, setShowVideo] = useState(false)
  return (
    <>
      <div
        className="fixed inset-0 bg-cover bg-center sm:hidden -z-10"
        style={{ backgroundImage: "url('/videos/bg-still.jpg')" }}
      />

      {!showVideo ? (
        <div
          className="fixed inset-0 bg-cover bg-center hidden sm:block -z-10"
          style={{ backgroundImage: "url('/videos/bg-still.jpg')" }}
        />
      ) : (
        <video
          autoPlay
          loop
          muted
          playsInline
          preload="auto"
          disablePictureInPicture
          className="fixed inset-0 w-full h-full object-cover hidden sm:block -z-10"
        >
          <source src="/videos/background-1080p.mp4" type="video/mp4" />
        </video>
      )}

      <div className={`fixed inset-0 -z-10`} style={{backgroundColor: `rgba(0, 0, 0, ${opacity / 100})`}}/>

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
              {showVideo ? "Disable Animation?" : "Enable Animation?"}
            </span>
            <Switch
              checked={showVideo}
              onCheckedChange={setShowVideo}
              id="animated-bg-switch"
            />
          </div>
        </PopoverContent>
      </Popover>
    </>
  )
}
