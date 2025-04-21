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



const regions = [
  "br1", "eun1", "euw1", "jp1", "kr", "la1", "la2",
  "me1", "na1", "oc1", "ru", "sg2", "tr1", "tw2", "vn2"
];
const routingRegionMap: Record<string, "americas" | "europe" | "asia"> = {
  na1: "americas",
  br1: "americas",
  la1: "americas",
  la2: "americas",
  oc1: "americas",
  kr: "asia",
  jp1: "asia",
  sg2: "asia",
  tw2: "asia",
  vn2: "asia",
  eun1: "europe",
  euw1: "europe",
  tr1: "europe",
  ru: "europe",
  me1: "europe",
};

export default function SearchForm() {
  const router = useRouter()
  const [riotId, setRiotId] = useState("")
  const [region, setRegion] = useState("na1"); 
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
      const routingRegion = routingRegionMap[region];
      const player = await searchPlayer(riotId, tagLine, region, routingRegion)
      router.push(`/player/${player.puuid}?region=${region}`)
    } catch (err) {
      setError("Failed to find player")
    } finally {
      setIsLoading(false)
    }
  }

  return (
      <form
        onSubmit={handleSubmit}
        className="space-y-6 p-6 bg-white/10 backdrop-blur-sm rounded-xl shadow-lg border border-white/30"
      >
      {error && (
        <Alert variant="destructive" className="bg-white/20 backdrop-blur-md mb-4">
          <AlertCircle className="h-4 w-4 mr-2" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="grid gap-2">
        <Label htmlFor="riotId" className="font-medium text-white">
          Riot ID
        </Label>
        <Input
          id="riotId"
          placeholder="TheBestMid"
          value={riotId}
          onChange={(e) => setRiotId(e.target.value)}
          disabled={isLoading}
          className="border text-white focus:border-blue-500 focus:ring focus:ring-blue-200"
        />
      </div>

      <div className="grid gap-2">
        <Label htmlFor="tagLine" className="font-medium text-white">
          Tagline
        </Label>
        <Input
          id="tagLine"
          placeholder="NA1"
          value={tagLine}
          onChange={(e) => setTagLine(e.target.value)}
          disabled={isLoading}
          className="border text-white focus:border-blue-500 focus:ring focus:ring-blue-200"
        />
      </div>
      <div className="grid gap-2">
        <Label htmlFor="region" className="font-medium text-white">
          Region
        </Label>
        <select
          id="region"
          value={region}
          onChange={(e) => setRegion(e.target.value)}
          disabled={isLoading}
          className="border text-gray-200 px-3 py-2 rounded-md focus:border-blue-500 focus:ring focus:ring-blue-200"
        >
          {regions.map((r) => (
            <option key={r} value={r} className="text-black">
              {r.toUpperCase()}
            </option>
          ))}
        </select>
      </div>

      <Button type="submit" className="cursor-pointer w-full bg-blue-600/70 hover:bg-blue-700 text-white" disabled={isLoading}>
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
