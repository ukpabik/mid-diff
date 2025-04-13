"use client"

import { useEffect, useState } from "react"
import { Card, CardContent } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { Loader2 } from "lucide-react"
import { getCachedMatches } from "@/lib/api"
import type { Match } from "@/lib/types"

export default function MatchHistoryClient({ puuid }: { puuid: string }) {
  const [matches, setMatches] = useState<Match[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Track the last time the user manually refreshed
  const [lastRefresh, setLastRefresh] = useState<number | null>(null)

  // 1) Initial load
  useEffect(() => {
    loadMatches()
  }, [puuid])

  // 2) Load matches function
  async function loadMatches() {
    try {
      setIsLoading(true)
      const data = await getCachedMatches(puuid)
      setMatches(data)
      setError(null)
    } catch (err) {
      console.error("Error loading matches:", err)
      setError(err instanceof Error ? err.message : "Failed to load matches")
    } finally {
      setIsLoading(false)
    }
  }

  // 3) Manual refresh – ensure at least 60s between clicks
  async function handleRefresh() {
    const now = Date.now()
    if (lastRefresh && now - lastRefresh < 60_000) {
      // too soon, do nothing or show alert
      return
    }
    setLastRefresh(now)
    await loadMatches()
  }

  // Figure out how many seconds until the refresh button is re-enabled
  let refreshDisabled = false
  let refreshWait = 0
  if (lastRefresh) {
    const elapsed = Date.now() - lastRefresh
    if (elapsed < 60_000) {
      refreshDisabled = true
      refreshWait = Math.ceil((60_000 - elapsed) / 1000)
    }
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center py-8">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    )
  }

  // If an error happened
  if (error) {
    return (
      <div className="text-center py-8">
        <p className="text-red-500">{error}</p>
      </div>
    )
  }

  // If no matches are found
  if (!matches || matches.length === 0) {
    return (
      <div className="text-center py-8">
        <p className="text-muted-foreground">No match history available</p>
        {/* Refresh button */}
        <button
          className="mt-4 px-4 py-2 bg-secondary text-secondary-foreground rounded disabled:opacity-50"
          onClick={handleRefresh}
          disabled={refreshDisabled}
        >
          {refreshDisabled
            ? `Wait ${refreshWait}s`
            : "Refresh"}
        </button>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {/* Refresh UI */}
      <div className="text-right">
        <button
          className="mb-2 px-4 py-2 bg-secondary text-secondary-foreground rounded disabled:opacity-50"
          onClick={handleRefresh}
          disabled={refreshDisabled}
        >
          {refreshDisabled
            ? `Wait ${refreshWait}s`
            : "Refresh Matches"}
        </button>
      </div>

      {matches.map((match) => (
        <MatchCard key={match.matchId} match={match} />
      ))}
    </div>
  )
}

function MatchCard({ match }: { match: Match }) {
  const isWin = match.win

  // Format match date
  const matchDate = new Date(match.gameStartTimestamp)
  const formattedDate = matchDate.toLocaleDateString()
  const formattedTime = matchDate.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })

  // Calculate total CS
  const totalCS = match.totalMinionsKilled + match.neutralMinionsKilled

  // Format game duration
  const minutes = Math.floor(match.gameDuration / 60)
  const seconds = match.gameDuration % 60
  const formattedDuration = `${minutes}m ${seconds}s`

  // Position
  const getPositionName = (position: string) => {
    switch (position?.toLowerCase()) {
      case "top":
        return "Top"
      case "jungle":
        return "Jungle"
      case "mid":
      case "middle":
        return "Mid"
      case "bottom":
      case "adc":
        return "ADC"
      case "utility":
      case "support":
        return "Support"
      default:
        return position || "Unknown"
    }
  }

  return (
    <Card className={`overflow-hidden border-l-4 ${isWin ? "border-l-green-500" : "border-l-red-500"}`}>
      <CardContent className="p-4">
        <div className="grid gap-4 md:grid-cols-[1fr_auto]">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <Badge variant={isWin ? "default" : "destructive"}>{isWin ? "Victory" : "Defeat"}</Badge>
              <span className="text-sm text-muted-foreground">
                {formattedDate} at {formattedTime}
              </span>
              <span className="text-sm text-muted-foreground">•</span>
              <span className="text-sm text-muted-foreground">{formattedDuration}</span>
            </div>

            <div className="flex flex-wrap items-center gap-2 mb-2">
              <div className="font-semibold">{match.championName}</div>
              {match.teamPosition && (
                <>
                  <span className="text-muted-foreground">•</span>
                  <div className="text-sm">{getPositionName(match.teamPosition)}</div>
                </>
              )}
              <span className="text-muted-foreground">•</span>
              <div className="text-sm">
                {match.kills}/{match.deaths}/{match.assists}
              </div>
              <div className="text-xs text-muted-foreground">{match.kda.toFixed(2)} KDA</div>
            </div>

            <Separator className="my-2" />

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-x-4 gap-y-2 mt-2">
              <div>
                <div className="text-xs text-muted-foreground">CS</div>
                <div className="text-sm">
                  {totalCS} ({match.csPerMin.toFixed(1)}/min)
                </div>
              </div>

              <div>
                <div className="text-xs text-muted-foreground">Vision</div>
                <div className="text-sm">{match.visionScore} score</div>
              </div>

              <div>
                <div className="text-xs text-muted-foreground">Damage</div>
                <div className="text-sm">{(match.damageDealtToChampions / 1000).toFixed(1)}k</div>
              </div>

              <div>
                <div className="text-xs text-muted-foreground">Gold</div>
                <div className="text-sm">{(match.goldEarned / 1000).toFixed(1)}k</div>
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
