import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"
import { ArrowLeft } from "lucide-react"
import PlayerCard from "@/components/player-card"
import MatchHistoryClient from "@/components/match-history-client"
import { getPlayerFromDb } from "@/lib/api"
import { notFound } from "next/navigation"

export const dynamic = "force-dynamic";

export default async function PlayerPage({
  params,
}: {
  params: { puuid: string };
}) {
  try {
    const { puuid } = params;
    let error = null;
    const player = await getPlayerFromDb(puuid)

    if (!player) {
      error = "Failed to load player";
      notFound()
    }


    return (
    <main className="container mx-auto px-4 py-8">
      <div className="mb-6">
        <Link href="/">
          <Button variant="outline" size="sm">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Back to Search
          </Button>
        </Link>
      </div>

      <div className="grid gap-8 md:grid-cols-[300px_1fr] lg:grid-cols-[350px_1fr]">
        <div>
          {!player && !error ? (
            <PlayerCardSkeleton />
          ) : error ? (
            <Card className="p-6">
              <div className="text-center text-red-500">{error}</div>
            </Card>
          ) : (
            <PlayerCard player={player} />
          )}
        </div>

        <div>
          <Card className="p-6">
            <h2 className="text-2xl font-bold mb-4">Match History</h2>
            {error ? (
              <div className="text-center text-red-500">Failed to load matches</div>
            ) : (
              <MatchHistoryClient puuid={params.puuid} />
            )}
          </Card>
        </div>
      </div>
    </main>
  )
  } catch (err) {
    console.error("Error loading player:", err)
    throw new Error("Failed to load player")
  }
}

function PlayerCardSkeleton() {
  return (
    <Card className="p-6">
      <div className="space-y-4">
        <Skeleton className="h-24 w-24 rounded-full mx-auto" />
        <Skeleton className="h-8 w-3/4 mx-auto" />
        <Skeleton className="h-6 w-1/2 mx-auto" />
        <div className="space-y-2">
          <Skeleton className="h-4 w-full" />
          <Skeleton className="h-4 w-full" />
          <Skeleton className="h-4 w-3/4" />
        </div>
      </div>
    </Card>
  )
}
