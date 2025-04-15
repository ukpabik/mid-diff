"use client"

import type React from "react"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { AlertCircle, Loader2 } from "lucide-react"
import { searchPlayer } from "@/lib/api"

export default function SearchForm() {
  const router = useRouter()
  const [riotId, setRiotId] = useState("")
  const [tagLine, setTagLine] = useState("")
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()

    if (!riotId || !tagLine) {
      setError("Please enter both Riot ID and tagline")
      return
    }

    setIsLoading(true)
    setError(null)

    try {
      const player = await searchPlayer(riotId, tagLine)
      router.push(`/player/${player.puuid}`)
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to find player")
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-6 p-4 bg-white rounded-lg shadow-sm">
      {error && (
        <Alert variant="destructive" className="bg-white mb-4">
          <AlertCircle className="h-4 w-4 mr-2" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="grid gap-2">
        <Label htmlFor="riotId" className="font-medium text-gray-700">
          Riot ID
        </Label>
        <Input
          id="riotId"
          placeholder="TheBestMid"
          value={riotId}
          onChange={(e) => setRiotId(e.target.value)}
          disabled={isLoading}
          className="border text-black focus:border-blue-500 focus:ring focus:ring-blue-200"
        />
      </div>

      <div className="grid gap-2">
        <Label htmlFor="tagLine" className="font-medium text-gray-700">
          Tagline
        </Label>
        <Input
          id="tagLine"
          placeholder="NA1"
          value={tagLine}
          onChange={(e) => setTagLine(e.target.value)}
          disabled={isLoading}
          className="border text-black focus:border-blue-500 focus:ring focus:ring-blue-200"
        />
      </div>

      <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 text-white" disabled={isLoading}>
        {isLoading ? (
          <div className="flex items-center justify-center">
            <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            Searching...
          </div>
        ) : (
          "Search Player"
        )}
      </Button>
    </form>
  );
}
