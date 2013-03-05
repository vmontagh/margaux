module schedule

open util/ordering[Time] as TimeOrd
open util/ordering[Day] as DayOrd
open util/ordering[Session] as SessionOrd

abstract sig Team {
    skills : one Session,
    practice : one Session,
    ice : set Session
}{
    -- every team gets two practices per week
    ice = skills + practice
}

sig Mite extends Team {}
sig Squirt extends Team {}
sig Peewee extends Team {}
sig Bantam extends Team {}
sig Midget extends Team {}
sig Girls extends Team {}
sig Goalies extends Team {}

abstract sig Day {
    //sched : Time -> Team
}
one sig Mon extends Day {}
one sig Tue extends Day {}
one sig Wed extends Day {}
one sig Thu extends Day {}

abstract sig Time {}
one sig Six extends Time {}
one sig Seven extends Time {}
one sig Eight extends Time {}

sig Session {
    day : one Day,
    time : one Time,
    teams : set Team
}

fun datavalue[] : Day->Time->Team {
    {d : Day, t : Time, m : Team | some s : Session | s.day=d and s.time=t and s.teams=m}
}

fact what { some datavalue }

fact sanity {
    // sessions are unique
    all s1, s2 : Session | s1.day = s2.day and s1.time = s2.time implies s1=s2

    // defined var
    teams = ~ ice
    // another defined var
    //sched = {d : Day, m : Time, t : Team | some s : Session | s.day = d && s.time = m && t in s.teams}

    // ordering of Times
    Six = TimeOrd/first
    Eight = TimeOrd/last

    // ordering of Days
    Mon = DayOrd/first
    Tue = DayOrd/next[Mon]
    Wed = DayOrd/next[Tue]
    Thu = DayOrd/last

    // ordering of Sessions
    all disjoint s1, s2 : Session | {
        s1.day in DayOrd/prevs[s2.day] implies s1 in SessionOrd/prevs[s2]
        s1.day = s2.day and s1.time in TimeOrd/prevs[s2.time] implies s1 in SessionOrd/prevs[s2]
    }
}

fact schedule {
    -- at most two teams on the ice
    all s : Session | #(ice.s) < 3

    -- a team never has two practices on the same day
    no t : Team | t.skills.day = t.practice.day


    // at most one session that's half-utilized
    //lone s : Session | #(ice.s) = 1

    -- Mites always on at Six
    (Mite.ice).time = Six
    -- Squirts never on at Eight
    Eight not in (Squirt.ice).time
    -- Bantams never on at Six
    Six not in (Bantam.ice).time
    -- Girls not on at Eight more than once
    all g : Girls | let t = (g.ice).time | Eight in t implies (Six in t or Seven in t)
    -- Midgets always on at Eight
    (Midget.ice).time = Eight

    -- Midgets do not share with Mites, Squirts, Peewees, or Girls
    no Midget.ice & (Mite + Squirt + Peewee + Girls).ice
    -- Bantams do not share with Mites, Squirts, or Girls
    no Bantam.ice & (Mite + Squirt + Girls).ice
    -- Peewees do not share with Mites
    no Peewee.ice & Mite.ice

   
    // Mites on Monday and Wednesday
    //(Mite.ice).day in (Mon + Wed)


    // Girls not on Thursday
    //Thu not in (Girls.ice).day

}

fact likeLastSeason {
    -- some Squirt team has ice on Monday and Wednesday
    (Mon + Wed) in (Squirt.ice).day
    -- some Mite team has ice on Monday and Wednesday
    (Mon + Wed) in (Mite.ice).day
}

pred restDay(T : set Team) {
    // everyone gets at least one day of rest between practices
    // skills don't come the day after practice
    no t : T | t.skills.day in DayOrd/next[t.practice.day]
    // practice doesn't come the day after skills
    no t : T | t.practice.day in DayOrd/next[t.skills.day]
}

pred together(T : set Team) {
    all disjoint t1, t2 : T | t1.skills = t2.skills and t1.practice = t2.practice
}

pred sept[] {}
pred nov[]{
    -- November:  Midgets together
    together[Midget]
}
pred winter[] {
    -- Winter:  Midgets and Bantams together
    together[Midget + Bantam]
}

pred fullSplit[] {
    -- Full Split: everyone gets at least one day of rest between practices
    restDay[Team]
    -- Full Split:  some Mite team on every day
    (Mon + Tue + Wed + Thu) in (Mite.ice).day
    -- Full Split:  some Squirt team on every day
    (Mon + Tue + Wed + Thu) in (Squirt.ice).day
    -- Full Split:  some Girls team on every day
    (Mon + Tue + Wed + Thu) in (Girls.ice).day
}

pred noSplit[] {
    -- No Split: everyone gets at least one day of rest between practices
    restDay[Team]
    -- No Split:  Mites together
    together[Mite]
    -- No Split:  Girls together
    together[Girls]
}

pred halfSplit() {
    -- Half Split:  everyone gets a rest day between practices except Squirts
    restDay[Team - Squirt]
    -- Half Split:  Mites together once, apart once
    all disjoint t1, t2 : Mite | t1.skills = t2.skills and t1.practice != t2.practice
    -- Half Split:  Girls together once, apart once
    all disjoint t1, t2 : Girls | t1.skills = t2.skills and t1.practice != t2.practice
}

pred novFullSplit() { nov[] and fullSplit[] }
pred winterFullSplit() { winter[] and fullSplit[] }

pred novHalfSplit() { nov[] and halfSplit[] }
pred winterHalfSplit() { winter[] and halfSplit[] }

pred novNoSplit() { nov[] and noSplit[] }
pred winterNoSplit() { winter[] and noSplit[] }


// 12 sessions can support 12 teams
run novNoSplit for exactly 2 Mite, exactly 2 Squirt, exactly 2 Peewee,  exactly 1 Bantam, exactly 2 Midget, exactly 2 Girls, exactly 0 Goalies, 
12 Session

run winterNoSplit for exactly 2 Mite, exactly 2 Squirt, exactly 2 Peewee, exactly 1 Bantam, exactly 1 Midget, exactly 2 Girls, exactly 1 Goalies, 
12 Session
