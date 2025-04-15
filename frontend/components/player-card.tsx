import { Card, CardContent, CardHeader } from "@/components/ui/card"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Badge } from "@/components/ui/badge"
import type { Player } from "@/lib/types"

interface PlayerCardProps {
  player: Player | null
}

export default function PlayerCard({ player }: PlayerCardProps) {
  if (!player) {
    return (
      <Card>
        <CardHeader className="flex flex-col items-center pb-2">
          <p>No player data available</p>
        </CardHeader>
      </Card>
    )
  }

  return (
    <Card>
      <CardHeader className="flex flex-col items-center pb-2">
      <Avatar className="h-24 w-24 mb-2">
          {player.profilePicture ? (
            <AvatarImage src={player.profilePicture} alt={player.gameName} />
          ) : (
            <AvatarFallback className="text-2xl">
              <AvatarImage src={"https://ddragon.leagueoflegends.com/cdn/13.23.1/img/profileicon/0.png"} />
            </AvatarFallback>
          )}
        </Avatar>
        <h2 className="text-2xl font-bold">{player.gameName}</h2>
        <div className="flex items-center gap-2">
          <Badge variant="outline">#{player.tagLine}</Badge>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-2 gap-2 text-sm">
          <div className="text-muted-foreground">PUUID</div>
          <div className="truncate font-mono text-xs" title={player.puuid}>
            {player.puuid.substring(0, 8)}...
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
