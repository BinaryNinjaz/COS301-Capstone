# Binary Ninjaz - Prize Justification

## EOH: A Microsoft Partner - Innovation

The Harvest system was made with people with basic computer literacy skills in mind. Targeting this audience has meant that our system must be elementary to begin. However, as people become accustomed to the system, we would like to enable them to become power users. To do this, we have aimed to use progressive disclosure in the user interface design of the system. Achieving this goal is crucial to us because we want to empower people who may not have the ability to use extensive systems initially.

Areas, where we have implemented progressive disclosure are information entering, statistics creation and search functionality.

To achieve our goal concerning search functionality, we found it necessary to make a domain specific language that can scale naturally with a user.

Some examples of search functionality:

- "Peter": shows all items that have some field that contains the word peter.
- "Peter or Jamie": shows all items that have some field that contains the word peter or Jamie.
- "Peter Jamie": shows all items that have some field that contains the word peter and some field (other or the same as Peter) that contains the word, Jamie.
- "foreman peter": shows all items with a foreman that is named Peter.
- "worker Jamie sum": shows all items with a worker called Jamie. Also shows the sum of the yield collected.
- "last 3 days in orchard block a": shows all sessions in the last three days that had work done in the orchard named "block A".

[Domain Specific Language](https://github.com/BinaryNinjaz/COS301-Capstone/blob/master/Source/Web/app/harvestQuery.js)

## Retrorabbit - Algorithmic Innovation

While developing our system we encountered challenges that required using specific algorithms. We would like to discuss three design challenges that required us to turn to specific algorithms.

### Definitions:
- Yield: The amount of produce collected on a farm.
- Session: Where workers collect produce for a period.

### Genetic Algorithm

One of the requirements for our system was to provide expected yields for farmers. This requirement proved to be challenging for a couple of reasons. We did not have any historical data of previous yields to base our models on if we were to use something such as a neural network. Secondly, There are many different considerations to take into account, factors such as farm size, crop and seasonal factors, which would mean we would need a flexible solution. To solve these problems we turned to use a genetic algorithm to help us minimise the error rate of a sinusoidal function.

[Genetic Algorithm](https://github.com/BinaryNinjaz/COS301-Capstone/blob/3021e1301d544b0dd904872b7d3ba144e9cb5c5e/Source/Web/functions/index.js#L296)

### Convex Hull

For a farmer to see how an orchard is performing they need to plot out an orchard on a map. However, people found performing this task not to be straightforward. So instead we decided we should build an inferred orchard area from the points of sessions that selected a particular orchard. We were able to produce great results for users using the Graham scan convex hull algorithm.

[Convex Hull](https://github.com/BinaryNinjaz/COS301-Capstone/blob/3021e1301d544b0dd904872b7d3ba144e9cb5c5e/Source/iOS/Harvest/Harvest/Model/Extensions/LocationHelper.swift#L104)

### Ray Casting

To reduce the chances of breaking referential integrity in our system we decided to make points not tied to anything other than a session. This decision meant that we needed to calculate where pickups were made. Hence, the problem essentially broke down to if a polygon contains a point. In computer graphics, this is a commonly known problem. Hence we used a ray casting algorithm.

[Ray Casting](https://github.com/BinaryNinjaz/COS301-Capstone/blob/3021e1301d544b0dd904872b7d3ba144e9cb5c5e/Source/Web/functions/index.js#L14)

## Qode Health (Pty) Ltd - Triple Bottom Line

Harvest aims to provide an easily accessible and cheap solution for farms of all sizes. While there are farms that have advanced tracking and monitoring of their workers and crops these are usually large farms that can easily afford to pay the costs involved.

The Harvest system is built to use as little resources as possible. We only rely on mobile GPS systems which are now very commonly found in phones. Along with an internet connection, there's nothing else one needs to start using the Harvest system.

As price is a concern, we needed to minimise internet costs. To do this, we employed caching on the mobile systems. Our use of a real-time database also meant we could have more fine grain control over downloads and uploads.

Continuing on the note of reducing costs we employed subsystems in Harvest to help prevent theft. Theft is a significant concern for many farmers who cannot always easily monitor their workers. The Harvest system has real-time location tracking of foremen during a working session. We provide stats to help see both underperforming workers and areas.

By monitoring staff, we can combat unproductive areas which may also combat theft and poor foreman management. Ultimately this system will help the companies financial growth by creating more productive farms that can expand and employ more workers as a consequence.
