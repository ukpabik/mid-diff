"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import type { Player, RankInfoEntry } from "@/lib/types";
import { getRankInfoFromDb } from "@/lib/api";

// Helper function to convert queue types to user-friendly display
function getQueueDisplayName(queueType: string): string {
  switch (queueType) {
    case "RANKED_SOLO_5x5":
      return "Ranked Solo/Duo";
    case "RANKED_FLEX_SR":
      return "Ranked Flex";
    default:
      return queueType;
  }
}

function getTierIconUrl(tier: string): string {
  return `/ranks/${tier.toLowerCase()}.png`;
}

// Child component to display a single rank entry
function RankCard({ entry }: { entry: RankInfoEntry }) {
  const queueName = getQueueDisplayName(entry.queueType);
  const tierIcon = getTierIconUrl(entry.tier);

  // Calculate win rate
  const totalGames = entry.wins + entry.losses;
  const winRate = totalGames > 0 ? Math.round((entry.wins / totalGames) * 100) : 0;

  return (
    <div className="border rounded-lg p-4 bg-black/80  flex flex-col gap-3">
      <div className="text-sm font-medium text-white/70 italic">{queueName}</div>
      <div className="flex items-center gap-4">
        <img src={tierIcon} alt={entry.tier} className="w-14 h-14" />
        <div>
          <div className="text-xl font-semibold">
            {entry.tier} {entry.player_rank}
          </div>
          <div className="text-sm">{entry.leaguePoints} LP</div>
        </div>
      </div>
      <div className="flex justify-between text-sm">
        <span>
          {entry.wins}W {entry.losses}L
        </span>
        <span>Win rate {winRate}%</span>
      </div>
    </div>
  );
}

interface PlayerCardProps {
  player: Player | null;
  region: string;
}

export default function PlayerCard({ player, region }: PlayerCardProps) {
  const [rankInfo, setRankInfo] = useState<RankInfoEntry[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchRankInfo() {
      if (player) {
        try {
          const ranks = await getRankInfoFromDb(player.puuid, region);
          const sortedRanks = [...ranks].sort((a, b) => {
            if (a.queueType === "RANKED_SOLO_5x5") return -1;
            if (b.queueType === "RANKED_SOLO_5x5") return 1;
            return 0;
          });
          setRankInfo(sortedRanks);
        } catch (e: any) {
          console.error("Error fetching rank info:", e);
          setError("Failed to fetch rank info");
        }
      }
    }
    fetchRankInfo();
  }, [player]);

  return (
    <Card className="bg-blue-300/70">
      <CardHeader className="flex flex-col items-center pb-2">
        <Avatar className="h-24 w-24 mb-2">
          {player?.profilePicture && player.profilePicture.trim().length > 0 ? (
            <AvatarImage src={player.profilePicture} alt={player.gameName} />
          ) : (
            <AvatarFallback className="text-2xl">
              <AvatarImage
                src="https://ddragon.leagueoflegends.com/cdn/13.23.1/img/profileicon/0.png"
                alt="Default Summoner Icon"
              />
            </AvatarFallback>
          )}
        </Avatar>
        <h2 className="text-2xl font-bold">{player?.gameName}</h2>
        <div className="flex items-center gap-2">
          <Badge className="bg-black/70" variant="outline">#{player?.tagLine}</Badge>
        </div>
        {error && <p className="text-red-500 text-sm mt-2">{error}</p>}
      </CardHeader>
      <CardContent>
        {rankInfo ? (
          <div className="space-y-4">
            {rankInfo.map((entry) => (
              <RankCard key={entry.queueType} entry={entry} />
            ))}
          </div>
        ) : (
          <p className="text-sm text-muted-foreground">Loading rank info...</p>
        )}
      </CardContent>
    </Card>
  );
}
